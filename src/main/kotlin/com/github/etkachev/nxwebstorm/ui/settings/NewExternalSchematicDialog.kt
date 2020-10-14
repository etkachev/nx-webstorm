package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class NewExternalSchematicDialog(private val existingList: Array<String>) : DialogWrapper(null) {
  var packageNameText: String = ""

  init {
    super.init()
    super.setTitle("New Item")
  }

  override fun createCenterPanel(): JComponent? {
    return panel {
      row {
        label("Enter package name to scan")
      }
      row {
        textField({ getText() }, { value -> setText(value) }).withValidationOnInput { field -> validateText(field) }
          .focused()
      }
    }
  }

  private fun validateText(field: JBTextField): ValidationInfo? {
    val emptyText = field.text.isNullOrEmpty()
    if (emptyText) {
      return ValidationInfo("Cannot be empty", field)
    }
    val alreadyExists = existingList.find { s -> s == field.text } ?: return null
    return ValidationInfo("$alreadyExists is already in use. Try something else.", field)
  }

  private fun getText(): String {
    return packageNameText
  }

  private fun setText(value: String) {
    packageNameText = value
  }
}
