package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.FormValueMap
import com.github.etkachev.nxwebstorm.utils.FormCombo
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class CheckboxListener(private val formValues: FormValueMap, private val formCombo: FormCombo) : ActionListener {
    override fun actionPerformed(e: ActionEvent?) {
        val existingValue = formValues.formVal[formCombo.name] ?: "false"
        val newValue = if (existingValue == "false") "true" else "false"
        formValues.setFormValueOfKey(formCombo.name, newValue)
    }
}