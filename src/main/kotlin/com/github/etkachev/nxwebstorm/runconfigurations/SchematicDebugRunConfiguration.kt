package com.github.etkachev.nxwebstorm.runconfigurations

import com.github.etkachev.nxwebstorm.models.RunSchematicConfig
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration

class SchematicDebugRunConfiguration(
  project: Project,
  factory: ConfigurationFactory,
  name: String,
  private val config: RunSchematicConfig
) : RunConfigurationBase<Any>(project, factory, name), NodeDebugRunConfiguration {
  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
    return SchematicDebugProfileState(environment, config)
  }

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
    return SchematicDebugSettingsEditor()
  }
}
