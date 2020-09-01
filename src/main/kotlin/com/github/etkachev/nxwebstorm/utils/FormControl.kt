package com.github.etkachev.nxwebstorm.utils

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent
import com.intellij.openapi.ui.ComboBox


enum class FormControlType {
  INVALID, BOOL, STRING, NUMBER, INTEGER, LIST
}

class FormCombo(
        val component: JComponent?,
        private val initialType: FormControlType,
        val name: String,
        val description: String?,
        val enums: Array<String>?,
        private val required: List<String>) {

  val type: FormControlType
    get() = if (enums != null) FormControlType.LIST else initialType

  val finalName: String
    get() = if (required.contains(name)) "$name *" else name

  val value: String?
    get() = if (component == null) null else when (component) {
        is JBTextField -> component.text
        is JBCheckBox -> if (component.isSelected) "true" else "false"
        is ComboBox<*> -> component.selectedItem.toString()
      else -> null
    }
}
