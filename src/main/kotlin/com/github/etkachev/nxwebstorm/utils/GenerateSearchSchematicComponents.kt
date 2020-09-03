package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.actionlisteners.SearchDocListener
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import javax.swing.DefaultListModel
import javax.swing.ListModel

fun getSearchTextField(items: List<Pair<String, String>>, jbList: JBList<String>): JBTextField {
  val field = JBTextField()
  field.document.addDocumentListener(SearchDocListener(field, items, jbList.model as DefaultListModel<String>))

  return field
}

fun getSchematicsListModel(items: List<Pair<String, String>>): ListModel<String> {
  val model = DefaultListModel<String>()
  for (s in items) {
    model.addElement(s.second)
  }
  return model
}
