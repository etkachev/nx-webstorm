package com.github.etkachev.nxwebstorm.actionlisteners

import com.intellij.ui.components.JBTextField
import javax.swing.DefaultListModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchDocListener(
  private val field: JBTextField,
  private val items: List<Pair<String, String>>,
  private val model: DefaultListModel<String>
) :
  DocumentListener {
  private fun search() {
    val value = field.text
    for (s in items) {
      val display = s.second
      if (!display.contains(value)) {
        if (model.contains(display)) {
          model.removeElement(display)
        }
      } else {
        if (!model.contains(display)) {
          model.addElement(display)
        }
      }
    }
  }

  override fun insertUpdate(e: DocumentEvent?) {
    search()
  }

  override fun removeUpdate(e: DocumentEvent?) {
    search()
  }

  override fun changedUpdate(e: DocumentEvent?) {
    search()
  }
}
