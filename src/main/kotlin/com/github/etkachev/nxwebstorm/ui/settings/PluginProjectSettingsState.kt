package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
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
  storages = [Storage("NxPluginProjectSettings.xml")]
)
class PluginProjectSettingsState : PersistentStateComponent<PluginProjectSettingsState?> {
  var nodeModulesFolder = "node_modules"
  var externalLibs = arrayOf(
    "$nodeModulesFolder/@nrwl/angular",
    "$nodeModulesFolder/@nrwl/nest",
    "$nodeModulesFolder/@nrwl/node",
    "$nodeModulesFolder/@nrwl/storybook",
    "$nodeModulesFolder/@nrwl/workspace",
    "$nodeModulesFolder/@schematics/angular",
    "$nodeModulesFolder/@nestjs/schematics",
    "$nodeModulesFolder/@ngrx/schematics"
  )
  var scanExplicitLibs = true
  var customSchematicsLocation = "/tools/schematics"

  override fun getState(): PluginProjectSettingsState? {
    return this
  }

  override fun loadState(state: PluginProjectSettingsState) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    val instance: PluginProjectSettingsState
      get() = ServiceManager.getService(PluginProjectSettingsState::class.java)
  }
}
