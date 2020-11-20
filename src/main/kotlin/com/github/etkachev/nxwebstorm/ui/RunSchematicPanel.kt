package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.CheckboxListener
import com.github.etkachev.nxwebstorm.actionlisteners.TextControlListener
import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicActionButtonPlacement
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.buttons.SchematicActionButtons
import com.github.etkachev.nxwebstorm.utils.FormCombo
import com.github.etkachev.nxwebstorm.utils.FormControlType
import com.github.etkachev.nxwebstorm.utils.GenerateFormControl
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.layout.panel
import javax.swing.BorderFactory
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.UIManager

class RunSchematicPanel(
  project: Project,
  private val id: String,
  schematicLocation: String,
  private val formMap: FormValueMap = FormValueMap()
) {
  var json = ReadFile.getInstance(project).readJsonFromFileUrl(schematicLocation)
  var required: JsonArray? = null
  private var formControlGenerator: GenerateFormControl
  private val nxService = MyProjectService.getInstance(project)

  init {
    required = if (json?.has("required") == true) json!!.get("required").asJsonArray else null
    formControlGenerator = GenerateFormControl(required, project)
  }

  private fun getWrappedTextAreaForLabel(label: String): JBTextArea {
    val textArea = JBTextArea(2, 15)
    textArea.text = label
    textArea.wrapStyleWord = true
    textArea.lineWrap = true
    textArea.isOpaque = false
    textArea.isEditable = false
    textArea.isFocusable = false
    textArea.background = UIManager.getColor("Label.background")
    textArea.font = UIManager.getFont("Label.font")
    textArea.border = UIManager.getBorder("Label.border")
    return textArea
  }

  private fun getFormControlKeys(props: JsonObject): List<FormCombo> {
    val formControls = props.keySet()
      .mapNotNull { key ->
        formControlGenerator.getFormControl(
          props.get(key).asJsonObject,
          key
        )
      }
    formControls.forEach { f -> formMap.setFormValueOfKey(f.name, f.value) }
    return formControls
  }

  private fun getActionGroup(
    runAction: () -> Unit,
    debugAction: () -> Unit,
    dryRunAction: () -> Unit
  ): List<ActionButton> {
    val actionGroup = DefaultActionGroup()
    actionGroup.add(SchematicActionButtons.run(runAction))
    actionGroup.add(SchematicActionButtons.debug(debugAction))
    actionGroup.add(SchematicActionButtons.dryRun(dryRunAction))
    val myActions = actionGroup.getChildren(null)
    val buttonList = mutableListOf<ActionButton>()
    for (action in myActions) {
      if (action is Separator) {
        continue
      }
      val presentation = action.templatePresentation
      val button = ActionButton(action, presentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
      buttonList.add(button)
    }
    return buttonList
  }

  private fun getFormRowData(control: FormCombo): FormRowData {
    val key = control.name
    val vg = formMap.valueGetter(key)
    val vs = formMap.valueSetter(key)
    val vbg = formMap.boolValueGetter(key)
    val vbs = formMap.boolValueSetter(key)
    val nvg = formMap.nullValueGetter(key)
    val nvs = formMap.nullValueSetter(key)
    val descriptionLabel = getWrappedTextAreaForLabel(control.description ?: "")
    return FormRowData(vg, vs, vbg, vbs, nvg, nvs, descriptionLabel)
  }

  fun generateCenterPanel(
    withBorder: Boolean = false,
    addButtons: Boolean = false,
    dryRunAction: () -> Unit = {},
    runAction: () -> Unit = {},
    debugAction: () -> Unit = {}
  ): JComponent? {
    val props = json?.get("properties")?.asJsonObject ?: return null
    val formControls = getFormControlKeys(props)
    val actions = this.getActionGroup(runAction, debugAction, dryRunAction)
    val (runBtn, debugBtn, dryRunBtn) = actions

    val panel = panel {
      if (addButtons && nxService.actionBarPlacement == SchematicActionButtonPlacement.TOP) {
        row {
          runBtn()
          dryRunBtn()
          debugBtn()
        }
      }
      row {
        label("ng generate workspace-schematic:$id")
      }
      formControls.mapNotNull { control ->
        val comp = control.component ?: return@mapNotNull null
        val (vg, vs, vbg, vbs, nvg, nvs, descriptionLabel) = getFormRowData(control)
        titledRow(control.finalName) {
          if (control.type != FormControlType.BOOL) {
            row { descriptionLabel() }
          }
          row {
            when (control.type) {
              FormControlType.LIST -> (comboBox<String>(
                DefaultComboBoxModel(control.enums), nvg,
                nvs
              ).component.editor.editorComponent as JTextField).document.addDocumentListener(
                TextControlListener(formMap, control)
              )
              FormControlType.STRING, FormControlType.INTEGER, FormControlType.NUMBER -> textField(
                vg,
                vs,
                25
              ).component.document.addDocumentListener(TextControlListener(formMap, control))
              FormControlType.BOOL -> checkBox(
                control.description ?: "", vbg, vbs
              ).component.addActionListener(CheckboxListener(formMap, control))
              FormControlType.AUTOCOMPLETE -> (comp().component as JTextField).document.addDocumentListener(
                TextControlListener(formMap, control)
              )
              else -> comp().onApply { formMap.setFormValueOfKey(control.name, control.value) }
            }
          }
        }
      }
      if (addButtons && nxService.actionBarPlacement == SchematicActionButtonPlacement.BOTTOM) {
        row {
          runBtn()
          dryRunBtn()
          debugBtn()
        }
      }
    }

    this.setBorder(panel, withBorder)
    return panel
  }

  private fun setBorder(panel: DialogPanel, withBorder: Boolean) {
    if (withBorder) {
      panel.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    }
  }
}

data class FormRowData(
  val valueGetter: () -> String,
  val valueSetter: (String) -> Unit,
  val boolValueGetter: () -> Boolean,
  val boolValueSetter: (Boolean) -> Unit,
  val nullValueGetter: () -> String?,
  val nullValueSetter: (String?) -> Unit,
  val descriptionLabel: JBTextArea
)
