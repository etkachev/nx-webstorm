package com.github.etkachev.nxwebstorm.runconfigurations

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.RunSchematicConfig
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import icons.PluginIcons

class SchematicDebugConfigurationType : SimpleConfigurationType(
  "node.debug.nx-schematics",
  "Nx Schematics",
  null,
  NotNullLazyValue.createValue { PluginIcons.NRWL_ICON }
) {
  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    val runConfig = RunSchematicConfig(CliCommands.NX, "generate", "", emptyMap())
    return SchematicDebugRunConfiguration(project, this, "Nx Schematics", runConfig)
  }

  companion object {
    fun getInstance(): SchematicDebugConfigurationType {
      return findConfigurationType(SchematicDebugConfigurationType::class.java)
    }

    fun getFactory(): ConfigurationFactory {
      val type = getInstance()
      return type.configurationFactories[0]
    }
  }
}
