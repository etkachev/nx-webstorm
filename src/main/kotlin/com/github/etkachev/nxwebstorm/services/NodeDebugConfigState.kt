package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.github.etkachev.nxwebstorm.utils.getCommandArguments
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import org.jdom.Attribute
import org.jdom.Document
import org.jdom.Element
import java.io.File

class NodeDebugConfigState(project: Project) {
  private val proj: Project = project
  private val readFile: ReadFile = ReadFile.getInstance(this.proj)
  private val runManagerName: String = "RunManager"
  private val configElementName: String = "configuration"
  private val componentElementName = "component"
  private val argsAttribute: String = "application-parameters"
  private val pathToJsFileAttribute: String = "path-to-js-file"
  private val workDirAttribute: String = "working-dir"
  private val nxDebugConfigName: String = "Nx.Debug.Config"
  private val nodeJsConfigTypeName: String = "NodeJSConfigurationType"
  private val nxService = MyProjectService.getInstance(this.proj)
  private val schematicsCliDir = nxService.schematicsCliDirectory
  private val workspacePath: String
    get() {
      val workspaceFile = proj.workspaceFile
      return workspaceFile?.path ?: proj.basePath + "/.idea/workspace.xml"
    }

  /**
   * execute and run debugger for your command
   */
  fun execute(command: String, name: String, args: Map<String, String>, type: SchematicTypeEnum) {
    val doc = this.addOrUpdateNxDebugConfig(command, name, args, type)
    this.saveWorkspaceFile(doc)
    this.runNodeDebug(command, name, args, type)
  }

  fun setupDebugConfig() {
    val cli = nxService.cliCommand
    ApplicationManager.getApplication().invokeLater {
      val doc = readWorkspaceFile()
      var hasExistingConfig = false
      var hasExistingRunManager = false
      for (content in doc.rootElement.children) {
        if (content.name == componentElementName && elementAttributesIsRunManager(content.attributes, runManagerName)) {
          hasExistingRunManager = true
          for (runManagerChild in content.children) {
            if (elementAttributesAreNxDebugConfig(runManagerChild, configElementName, nxDebugConfigName)) {
              hasExistingConfig = true
            }
          }
          if (!hasExistingConfig) {
            content.addContent(generateEmptyNxDebugConfig(cli))
          }
        }
      }

      if (!hasExistingRunManager) {
        doc.rootElement.addContent(this.generateEmptyRunManager(cli))
      }
      /**
       * only save if we added new config
       */
      if (!hasExistingConfig || !hasExistingRunManager) {
        this.saveWorkspaceFile(doc)
      }
    }
  }

  /**
   * load the workspace.xml document file
   */
  private fun readWorkspaceFile(): Document {
    return JDOMUtil.loadDocument(File(workspacePath))
  }

  /**
   * save the document to the workspace.xml
   */
  private fun saveWorkspaceFile(document: Document): Document? {
    return this.readFile.saveXml(document, workspacePath)
  }

  private fun setNxConfigAttributes(
    element: Element,
    command: String,
    name: String,
    args: Map<String, String>,
    type: SchematicTypeEnum
  ) {
    val cli = nxService.cliCommand
    for (runManagerChild in element.children) {
      if (elementAttributesAreNxDebugConfig(runManagerChild, configElementName, nxDebugConfigName)) {
        runManagerChild.setAttribute(argsAttribute, joinArgsWithCommand(command, name, args))
        this.setDirAttrBySchematicType(type, runManagerChild, cli)
      }
    }
  }

  private fun setDirAttrBySchematicType(
    type: SchematicTypeEnum,
    runManagerChild: Element,
    cli: CliCommands,
    initialSetup: Boolean = true
  ) {
    if (type == SchematicTypeEnum.CUSTOM_ANGULAR) {
      runManagerChild.setAttribute(workDirAttribute, "$schematicsCliDir/bin")
      runManagerChild.setAttribute(pathToJsFileAttribute, "schematics.js")
    } else {
      val (path, exec) = cli.data
      val projDir = if (initialSetup) "\$PROJECT_DIR\$" else this.proj.basePath
      val baseDir = if (nxService.configuredRootPath == "/") "/" else "/${nxService.configuredRootPath}/"
      runManagerChild.setAttribute(pathToJsFileAttribute, exec)
      runManagerChild.setAttribute(workDirAttribute, "$projDir$baseDir$path")
    }
  }

  /**
   * reads current workspace.xml and update the RunManager component
   * config to update existing Nx.debug config with new arguments.
   */
  private fun addOrUpdateNxDebugConfig(
    command: String,
    name: String,
    args: Map<String, String>,
    type: SchematicTypeEnum
  ): Document {
    val docFile = readWorkspaceFile()
    for (content in docFile.rootElement.children) {
      if (isComponentRunManager(content, this.componentElementName, runManagerName)) {
        this.setNxConfigAttributes(content, command, name, args, type)
      }
    }
    return docFile
  }

  /**
   * generates new nx.debug config with correct cli root command, but empty args for now
   */
  private fun generateEmptyNxDebugConfig(cli: CliCommands): Element {
    val newConfig = Element(configElementName)
    val cliPath = cli.data.path
    val rootDir = if (nxService.configuredRootPath == "/") "/" else "/${nxService.configuredRootPath}/"
    val attributes = listOf(
      Attribute("name", nxDebugConfigName),
      Attribute("type", nodeJsConfigTypeName),
      Attribute(argsAttribute, ""),
      Attribute(pathToJsFileAttribute, cli.data.exec),
      Attribute(workDirAttribute, "\$PROJECT_DIR\$$rootDir$cliPath")
    )
    newConfig.attributes = attributes
    val methodEl = Element("method")
    methodEl.setAttribute("v", "2")
    newConfig.addContent(methodEl)
    return newConfig
  }

  private fun generateEmptyRunManager(cli: CliCommands): Element {
    val newConfig = Element(componentElementName)
    val attributes = listOf(Attribute("name", runManagerName))
    newConfig.attributes = attributes
    newConfig.addContent(generateEmptyNxDebugConfig(cli))
    return newConfig
  }

  /**
   * find the Nx.Debug config and run it using the debug executor.
   */
  private fun runNodeDebug(command: String, name: String, args: Map<String, String>, type: SchematicTypeEnum) {
    ApplicationManager.getApplication().invokeLater {
      val runManager = RunManager.getInstance(this.proj)
      val allSettings = runManager.allSettings
      val match = allSettings.find { setting -> setting.name == nxDebugConfigName }
      if (match != null) {
        if (match.configuration is LocatableConfigurationBase<*>) {
          /**
           * workspace xml is setup so that these castings should pass.
           */
          @Suppress("UNCHECKED_CAST")
          val currentState = (match.configuration as LocatableConfigurationBase<Element>).state!!
          currentState.setAttribute(argsAttribute, joinArgsWithCommand(command, name, args))
          this.setDirAttrBySchematicType(type, currentState, nxService.cliCommand, false)
          @Suppress("UNCHECKED_CAST")
          (match.configuration as RunConfigurationBase<Element>).loadState(currentState)
        }
        ProgramRunnerUtil.executeConfiguration(match, DefaultDebugExecutor.getDebugExecutorInstance())
      }
    }
  }

  companion object {
    fun getInstance(project: Project): NodeDebugConfigState {
      return project.getService(NodeDebugConfigState::class.java)
    }
  }
}

internal fun elementAttributesIsRunManager(attributes: List<Attribute>, runManagerName: String): Boolean {
  return attributes.find { ca -> ca.name == "name" && ca.value == runManagerName } != null
}

internal fun elementAttributesAreNxDebugConfig(
  element: Element,
  configElementName: String,
  nxDebugConfigName: String
): Boolean {
  val nameIsConfig = element.name == configElementName
  val isNxDebugConfig = element.attributes.find { rc -> rc.name == "name" && rc.value == nxDebugConfigName } != null
  return nameIsConfig && isNxDebugConfig
}

/**
 * combine the command, name, along with arguments into string to execute.
 */
internal fun joinArgsWithCommand(command: String, name: String, args: Map<String, String>): String {
  return "$command $name " + getCommandArguments(args).joinToString(" ")
}

internal fun isComponentRunManager(element: Element, componentElementName: String, runManagerName: String): Boolean {
  val isComponent = element.name == componentElementName
  val isRunManager = element.attributes.find { ca -> ca.name == "name" && ca.value == runManagerName } != null
  return isComponent && isRunManager
}
