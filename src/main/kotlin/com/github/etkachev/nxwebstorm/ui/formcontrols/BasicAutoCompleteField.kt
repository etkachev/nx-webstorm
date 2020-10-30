package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.github.etkachev.nxwebstorm.models.SearchTableModel
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.JComponent

class BasicAutoCompleteField(list: Array<String>) {
  var table: StringListTable = StringListTable(list)
  var runnable = Runnable { this.setItemSelection() }
  var searchTable: SearchTableModel? = null

  fun createComponent(default: String?): JComponent? {
    this.searchTable = this.table.getTable(default ?: "")
    val st = this.searchTable!!
    val chooser = JBPopupFactory.getInstance().createPopupChooserBuilder(st.table)
    val focusListener = AutoCompleteFocusListenerPopup(chooser, st.field, this.runnable)
    st.field.addFocusListener(focusListener)
    st.field.addKeyListener(AutoCompleteKeyUpDownListener(st.table, st.field, focusListener))

    return st.field
  }

  private fun setItemSelection() {
    val table = this.searchTable!!.table
    val field = this.searchTable!!.field
    val selectedRow = table.selectedRow
    if (selectedRow == -1) {
      return
    }
    val selection = table.getValueAt(selectedRow, 0).toString()
    field.text = selection
  }
}
