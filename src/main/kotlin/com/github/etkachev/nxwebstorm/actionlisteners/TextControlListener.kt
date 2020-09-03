package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.utils.FormCombo
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TextControlListener(private val formValues: FormValueMap, private val formCombo: FormCombo) : DocumentListener {
  override fun insertUpdate(e: DocumentEvent?) {
    run(e)
  }

  override fun removeUpdate(e: DocumentEvent?) {
    run(e)
  }

  override fun changedUpdate(e: DocumentEvent?) {
    run(e)
  }

  private fun run(e: DocumentEvent?) {
    if (e == null) {
      return
    }
    val doc = e.document
    val value = doc.getText(0, doc.length)
    formValues.setFormValueOfKey(formCombo.name, value)
  }
}
