package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.github.etkachev.nxwebstorm.utils.getCommandArguments
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.impl.RunManagerImpl
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
  private val argsAttribute: String = "application-parameters"
  private val nxDebugConfigName: String = "Nx.Debug.Config"
  private val nodeJsConfigTypeName: String = "NodeJSConfigurationType"
  private val workspacePath: String
    get() {
      val workspaceFile = proj.workspaceFile
      return workspaceFile?.path ?: proj.basePath + "/.idea/workspace.xml"
    }

  /**
   * execute and run debugger for your command
   */
  fun execute(command: String, name: String, args: Map<String, String>) {
    val doc = this.addOrUpdateNxDebugConfig(command, name, args)
    this.saveWorkspaceFile(doc)
    val runManager = RunManager.getInstance(this.proj)
    (runManager as RunManagerImpl).copyTemplatesToProjectFromTemplate(this.proj)
    this.runNodeDebug(command, name, args)
  }

  fun setupDebugConfig() {
    val cli = MyProjectService.getInstance(this.proj).cliCommand
    ApplicationManager.getApplication().invokeLater {
      val doc = readWorkspaceFile()
      var hasExistingConfig = false
      for (content in doc.rootElement.children) {
        if (content.name == "component" && content.attributes.find { ca -> ca.name == "name" && ca.value == runManagerName } != null) {
          for (runManagerChild in content.children) {
            if (runManagerChild.name == configElementName && runManagerChild.attributes.find { rc -> rc.name == "name" && rc.value == nxDebugConfigName } != null) {
              hasExistingConfig = true
            }
          }
          if (!hasExistingConfig) {
            content.addContent(generateEmptyNxDebugConfig(cli))
          }
        }
      }
      /**
       * only save if we added new config
       */
      if (!hasExistingConfig) {
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

  /**
   * combine the command, name, along with arguments into string to execute.
   */
  private fun joinArgsWithCommand(command: String, name: String, args: Map<String, String>): String {
    return "$command $name " + getCommandArguments(args).joinToString(" ")
  }

  /**
   * reads current workspace.xml and update the RunManager component config to either update existing Nx.debug config with new arguments.
   */
  private fun addOrUpdateNxDebugConfig(command: String, name: String, args: Map<String, String>): Document {
    val docFile = readWorkspaceFile()
    // var hadExistingConfig = false
    for (content in docFile.rootElement.children) {
      if (content.name == "component" && content.attributes.find { ca -> ca.name == "name" && ca.value == runManagerName } != null) {
        for (runManagerChild in content.children) {
          if (runManagerChild.name == configElementName && runManagerChild.attributes.find { rc -> rc.name == "name" && rc.value == nxDebugConfigName } != null
          ) {
            runManagerChild.setAttribute(argsAttribute, joinArgsWithCommand(command, name, args))
            // hadExistingConfig = true
          }
        }
        // if (!hadExistingConfig) {
        //   content.addContent(generateNewNxDebugConfig(command, name, args))
        // }
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
    val attributes = listOf(
      Attribute("name", nxDebugConfigName),
      Attribute("type", nodeJsConfigTypeName),
      Attribute(argsAttribute, ""),
      Attribute("path-to-js-file", cli.data.exec),
      Attribute("working-dir", "\$PROJECT_DIR\$/$cliPath")
    )
    newConfig.attributes = attributes
    val methodEl = Element("method")
    methodEl.setAttribute("v", "2")
    newConfig.addContent(methodEl)
    return newConfig
  }

  /**
   * generate new nx debug config as NodeJSConfigurationType using arguments passed in.
   */
  private fun generateNewNxDebugConfig(command: String, name: String, debugArgs: Map<String, String>): Element {
    val newConfig = Element(configElementName)
    val attributes = listOf(
      Attribute("name", nxDebugConfigName),
      Attribute("type", nodeJsConfigTypeName),
      Attribute(argsAttribute, joinArgsWithCommand(command, name, debugArgs)),
      Attribute("path-to-js-file", "nx.js"),
      Attribute("working-dir", "\$PROJECT_DIR\$/node_modules/@nrwl/cli/bin")
    )
    newConfig.attributes = attributes
    val methodEl = Element("method")
    methodEl.setAttribute("v", "2")
    newConfig.addContent(methodEl)
    return newConfig
  }

  /**
   * find the Nx.Debug config and run it using the debug executor.
   */
  private fun runNodeDebug(command: String, name: String, args: Map<String, String>) {
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
