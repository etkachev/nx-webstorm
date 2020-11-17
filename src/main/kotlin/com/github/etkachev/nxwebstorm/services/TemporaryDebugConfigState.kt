package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.utils.getCommandArguments
import com.intellij.openapi.project.Project
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import com.intellij.openapi.util.JDOMUtil
import org.jdom.Attribute
import org.jdom.Document
import org.jdom.Element

class TemporaryDebugConfigState(project: Project) {
  private val proj: Project = project
  private val runManagerName: String = "RunManager"
  private val configElementName: String = "configuration"
  private val argsAttribute: String = "application-parameters"
  private val nxDebugConfigName: String = "Nx.Debug.Config"
  private val factory = DocumentBuilderFactory.newInstance()
  private val builder = factory.newDocumentBuilder()
  private val workspacePath: String
    get() {
      val workspaceFile = proj.workspaceFile
      return workspaceFile?.path ?: proj.basePath + "/.idea/workspace.xml"
    }

  fun testing(args: Map<String, String>) {
    val doc = this.addOrUpdateNxDebugConfig(args)
    this.saveWorkspaceFile(doc)
  }

  private fun readWorkspaceFile(): Document {
    return JDOMUtil.loadDocument(File(workspacePath))
  }

  private fun saveWorkspaceFile(document: Document) {
    JDOMUtil.writeDocument(document, workspacePath, "\n")
  }

  private fun joinArgs(args: Map<String, String>): String {
    return getCommandArguments(args).joinToString(" ")
  }

  private fun addOrUpdateNxDebugConfig(args: Map<String, String>): Document {
    val docFile = readWorkspaceFile()
    var hadExistingConfig = false
    for (content in docFile.rootElement.children) {
      if (content.name == "component" && content.attributes.find { ca -> ca.name == "name" && ca.value == runManagerName } != null) {
        for (runManagerChild in content.children) {
          if (runManagerChild.name == configElementName && runManagerChild.attributes.find { rc -> rc.name == "name" && rc.value == nxDebugConfigName } != null
          ) {
            runManagerChild.setAttribute(argsAttribute, joinArgs(args))
            hadExistingConfig = true
          }
        }
        if (!hadExistingConfig) {
          content.addContent(generateNewNxDebugConfig(args))
        }
      }
    }
    return docFile
  }

  private fun generateNewNxDebugConfig(debugArgs: Map<String, String>): Element {
    val newConfig = Element(configElementName)
    val attributes = listOf(
      Attribute("name", nxDebugConfigName),
      Attribute("type", "NodeJSConfigurationType"),
      Attribute(argsAttribute, joinArgs(debugArgs)),
      Attribute("path-to-js-file", "nx.js"),
      Attribute("working-dir", "\$PROJECT_DIR\$/node_modules/@nrwl/cli/bin")
    )
    newConfig.attributes = attributes
    val methodEl = Element("method")
    methodEl.setAttribute("v", "2")
    newConfig.addContent(methodEl)
    return newConfig
  }

  companion object {
    fun getInstance(project: Project): TemporaryDebugConfigState {
      return project.getService(TemporaryDebugConfigState::class.java)
    }
  }
}
