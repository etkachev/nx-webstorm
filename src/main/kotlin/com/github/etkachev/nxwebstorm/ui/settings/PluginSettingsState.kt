package com.github.etkachev.nxwebstorm.ui.settings

import com.github.etkachev.nxwebstorm.models.SchematicActionButtonPlacement
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
  name = "com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState",
  storages = [Storage("NxPluginSettings.xml")]
)
class PluginSettingsState : PersistentStateComponent<PluginSettingsState?> {
  var externalLibs = arrayOf(
    "@nrwl/angular",
    "@nrwl/nest",
    "@nrwl/node",
    "@nrwl/storybook",
    "@nrwl/workspace",
    "@schematics/angular",
    "@nestjs/schematics",
    "@ngrx/schematics",
    "@nrwl/react",
    "@nrwl/web",
    "@nrwl/gatsby",
    "@nrwl/bazel",
    "@nrwl/express",
    "@nrwl/next",
    "@nrwl/nx-plugin"
  ).joinToString(", ")
  var scanExplicitLibs = true
  var isPnpm = false
  var schematicActionButtonsPlacement = SchematicActionButtonPlacement.TOP.data

  override fun getState(): PluginSettingsState? {
    return this
  }

  override fun loadState(state: PluginSettingsState) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    val instance: PluginSettingsState
      get() = ApplicationManager.getApplication().getService(PluginSettingsState::class.java)
  }
}
