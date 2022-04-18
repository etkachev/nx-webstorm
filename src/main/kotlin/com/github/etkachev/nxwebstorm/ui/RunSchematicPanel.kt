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
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.BorderFactory
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField

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

  private fun getFormControlKeys(): List<FormCombo>? {
    val props = json?.get("properties")?.asJsonObject ?: return null
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
    return FormRowData(vg, vs, vbg, vbs, nvg, nvs)
  }

  fun generateCenterPanel(
    withBorder: Boolean = false,
    addButtons: Boolean = false,
    dryRunAction: () -> Unit = {},
    runAction: () -> Unit = {},
    debugAction: () -> Unit = {}
  ): JComponent? {
    val formControls = getFormControlKeys() ?: return null
    val actions = this.getActionGroup(runAction, debugAction, dryRunAction)
    val (runBtn, debugBtn, dryRunBtn) = actions

    val panel = panel {
      if (addButtons && nxService.actionBarPlacement == SchematicActionButtonPlacement.TOP) {
        row {
          cell(runBtn)
          cell(dryRunBtn)
          cell(debugBtn)
        }
      }
      row {
        label("ng generate workspace-schematic:$id")
      }
      formControls.mapNotNull { control ->
        val comp = control.component ?: return@mapNotNull null
        val (vg, vs, vbg, vbs, nvg, nvs) = getFormRowData(control)
        group(control.finalName) {
          row { cell() }.rowComment(control.description ?: "")
          row {
            when (control.type) {
              FormControlType.LIST ->
                (
                  comboBox(DefaultComboBoxModel(control.enums)).bindItem(nvg, nvs).component.editor.editorComponent as JTextField
                  ).document.addDocumentListener(TextControlListener(formMap, control))
              FormControlType.STRING, FormControlType.INTEGER, FormControlType.NUMBER -> textField()
                .bindText(vg, vs).component.document.addDocumentListener(TextControlListener(formMap, control))
              FormControlType.BOOL -> (checkBox(
                "Enabled",
              ).bindSelected(vbg, vbs).resizableColumn().component.addActionListener(CheckboxListener(formMap, control)))
              FormControlType.AUTOCOMPLETE -> (cell(comp).component as JTextField).document.addDocumentListener(
                TextControlListener(formMap, control)
              )
              else -> cell(comp).onApply { formMap.setFormValueOfKey(control.name, control.value) }
            }
          }
        }
      }
      if (addButtons && nxService.actionBarPlacement == SchematicActionButtonPlacement.BOTTOM) {
        row {
          cell(runBtn)
          cell(dryRunBtn)
          cell(debugBtn)
        }
      }
    }

    return this.setBorder(panel, withBorder)
  }

  private fun setBorder(panel: DialogPanel, withBorder: Boolean): DialogPanel {
    if (withBorder) {
      panel.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    }

    return panel
  }
}

data class FormRowData(
  val valueGetter: () -> String,
  val valueSetter: (String) -> Unit,
  val boolValueGetter: () -> Boolean,
  val boolValueSetter: (Boolean) -> Unit,
  val nullValueGetter: () -> String?,
  val nullValueSetter: (String?) -> Unit,
)
