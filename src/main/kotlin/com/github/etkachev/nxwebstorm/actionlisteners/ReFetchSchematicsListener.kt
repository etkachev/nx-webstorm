package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.SchematicsListToolTab
import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory

class ReFetchSchematicsListener(
  private val toolWindow: ToolWindow,
  private val contentFactory: ContentFactory,
  private val tabName: String,
  private val schematicToolTab: SchematicsListToolTab,
  private val schematicFetcher: FindAllSchematics
) {

  /**
   * gets the action listener re-fetching schematics.
   */
  fun getActionListener(removeOldListener: () -> Unit): () -> Unit {
    return { fetchSchematics(removeOldListener) }
  }

  /**
   * will remove the old listener and grab all the schematics again,
   * and re-adding it to the tool-window content
   */
  private fun fetchSchematics(removeOldListener: () -> Unit) {
    removeOldListener()
    val allSchematics = schematicFetcher.findAll()
    val listPanel = schematicToolTab.createCenterPanel(allSchematics)
    val content = contentFactory.createContent(listPanel, tabName, false)
    toolWindow.contentManager.removeAllContents(true)
    toolWindow.contentManager.addContent(content)
  }
}
