package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.github.etkachev.nxwebstorm.models.SchematicActionButtonPlacement
import com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState
import com.intellij.openapi.project.Project
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.google.gson.JsonObject
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder
import com.intellij.javascript.nodejs.settings.NodeInstalledPackage
import com.intellij.javascript.nodejs.settings.NodePackageManagementService
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer

class MyProjectService(private val project: Project) {
  private var readFile = ReadFile.getInstance(project)
  private var schematicsCliDir: String? = null
  private var projectRoot = ProjectRootManager.getInstance(project).contentRoots[0]
  val schematicsCliDirectory: String?
    get() = this.schematicsCliDir
  val nxJson: JsonObject?
    get() = readNxJson()
  val angularJson: JsonObject?
    get() = readAngularJson()
  val rootPath: String?
    get() {
      val thisProject = this.project
      return thisProject.basePath
    }

  private val nxCliVersion: SemVer?
    get() = NodeInstalledPackageFinder(this.project, this.projectRoot).findInstalledPackage("@nrwl/cli")?.version

  private val isNx11OrAbove: Boolean
    get() = this.nxCliVersion != null && this.nxCliVersion!!.major >= 11

  val defaultCustomSchematicsLocation: String
    get() = if (this.isNx11OrAbove) "/tools/generators" else "/tools/schematics"

  /**
   * full list of projects/libraries within current project.
   */
  val projectList: Array<String>
    get() {
      return this.getProjects()
    }

  /**
   * whether or not this project is nx project.
   */
  val isValidNxProject: Boolean
    get() = this.nxJson != null

  /**
   * whether this is an angular project.
   */
  val isAngularProject: Boolean
    get() = this.angularJson != null

  val nxProjectType: NxProjectType
    get() {
      return if (isValidNxProject) {
        NxProjectType.Nx
      } else if (isAngularProject) {
        NxProjectType.Angular
      } else {
        NxProjectType.Unknown
      }
    }

  /**
   * returns meta data on whether you run nx command or just plain ng command for angular projects.
   */
  val cliCommand: CliCommands
    get() {
      return if (this.nxProjectType == NxProjectType.Nx) CliCommands.NX else CliCommands.NG
    }

  /**
   * where the schematic action buttons should be placed on ui window.
   */
  val actionBarPlacement: SchematicActionButtonPlacement
    get() {
      return when (PluginSettingsState.instance.schematicActionButtonsPlacement) {
        SchematicActionButtonPlacement.TOP.data -> SchematicActionButtonPlacement.TOP
        SchematicActionButtonPlacement.BOTTOM.data -> SchematicActionButtonPlacement.BOTTOM
        else -> SchematicActionButtonPlacement.TOP
      }
    }

  private var alreadySetupNxDebugConfig = false

  val nxDebugConfigSetup: Boolean
    get() = this.alreadySetupNxDebugConfig

  init {
    val interpreter = NodeJsInterpreterManager(project).interpreter
    if (interpreter != null) {
      val installed = NodePackageManagementService(project, interpreter).installedPackages
      val schematicsPackage = installed.find { p -> p.name == "@angular-devkit/schematics-cli" }
      if (schematicsPackage != null) {
        this.schematicsCliDir = (schematicsPackage as NodeInstalledPackage).sourceRootDir.absolutePath
      }
    }
  }

  fun setNxDebugConfigSetupDone() {
    this.alreadySetupNxDebugConfig = true
  }

  private fun readNxJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("nx.json")
  }

  private fun readAngularJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("angular.json")
  }

  private fun readNxJsonFile(): VirtualFile? {
    return readFile.findVirtualFile("nx.json")
  }

  private fun getProjects(): Array<String> {
    if (this.isValidNxProject) {
      val json = this.nxJson ?: return emptyArray()

      val projects = json.getAsJsonObject("projects").keySet()
      return projects.toTypedArray()
    } else if (this.isAngularProject) {
      val angularJson = this.angularJson ?: return emptyArray()
      val angularProjects = angularJson.getAsJsonObject("projects").keySet()
      return angularProjects.toTypedArray()
    }
    return emptyArray()
  }

  companion object {
    fun getInstance(project: Project): MyProjectService {
      return project.getService(MyProjectService::class.java)
    }
  }
}
