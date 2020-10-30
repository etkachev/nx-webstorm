package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class AutoCompleteFocusListenerPopup(
  private val chooser: PopupChooserBuilder<Any>,
  private val field: JBTextField,
  private val selectionRunnable: Runnable
) : FocusListener {
  var popup: JBPopup? = null
  val popupExists: Boolean
    get() = this.popup != null && !this.popup!!.isDisposed

  override fun focusGained(e: FocusEvent?) {
    this.openPopup()
  }

  fun openPopup() {
    popup = chooser.createPopup()
    chooser.setItemChoosenCallback(selectionRunnable)
    chooser.scrollPane.preferredSize = Dimension(300, 300)
    popup!!.setRequestFocus(false)
    popup!!.showUnderneathOf(field)
  }

  override fun focusLost(e: FocusEvent?) {
    this.closePopup()
  }

  fun closePopup() {
    if (this.popup == null || this.popup!!.isDisposed) {
      return
    }

    this.popup!!.closeOk(null)
    this.popup = null
  }
}
