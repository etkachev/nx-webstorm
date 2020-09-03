package com.github.etkachev.nxwebstorm.utils

import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.util.ArrayList
import java.util.function.BiPredicate
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchListDecorator {
  fun <T> decorate(jList: JList<T>, entryFilter: BiPredicate<T, String?>): JPanel {
    require(jList.model is DefaultListModel<*>) { "List model must be an instance of DefaultListModel" }
    val model = jList.model as DefaultListModel<T>
    val items = getItems(model)
    val textField = JTextField()
    textField.document.addDocumentListener(object : DocumentListener {
      override fun insertUpdate(e: DocumentEvent?) {
        filter()
      }

      override fun removeUpdate(e: DocumentEvent?) {
        filter()
      }

      override fun changedUpdate(e: DocumentEvent?) {
        filter()
      }

      private fun filter() {
        model.clear()
        val s = textField.text
        for (item in items) {
          if (entryFilter.test(item, s)) {
            model.addElement(item)
          }
        }
      }
    })
    val panel = JPanel(BorderLayout())
    panel.add(textField, BorderLayout.NORTH)
    val pane = JBScrollPane(jList)
    panel.add(pane)
    return panel
  }

  private fun <T> getItems(model: DefaultListModel<T>): List<T> {
    val list: MutableList<T> = ArrayList()
    for (i in 0 until model.size()) {
      list.add(model.elementAt(i))
    }
    return list
  }
}
