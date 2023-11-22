package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.github.etkachev.nxwebstorm.models.SchematicActionButtonPlacement
import com.github.etkachev.nxwebstorm.ui.settings.PluginProjectSettingsState
import com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState
import com.intellij.openapi.project.Project
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.google.gson.JsonObject
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder
import com.intellij.javascript.nodejs.settings.NodeInstalledPackage
import com.intellij.javascript.nodejs.settings.NodePackageManagementService
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.text.SemVer
import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
  private var readFile = ReadFile.getInstance(project)
  private var schematicsCliDir: String? = null
  private var projectRoot = ProjectRootManager.getInstance(project).contentRootsFromAllModules[0]
  val schematicsCliDirectory: String?
    get() = this.schematicsCliDir
  val nxJson: JsonObject?
    get() = readNxJson()
  val angularJson: JsonObject?
    get() = readAngularJson()
  val workspaceJson: JsonObject?
    get() = readAngularJson() ?: readWorkspaceJson()
  val rootPath: String?
    get() {
      val thisProject = this.project
      return thisProject.basePath
    }

  val configuredRootPath: String
    get() {
      val projSettings = PluginProjectSettingsState.getInstance(this.project)
      val trimmedRoot = projSettings.rootDirectory.trim()
      var cleanedRoot = trimmedRoot

      if (cleanedRoot == "/") {
        return "/"
      }

      if (cleanedRoot.startsWith("/")) {
        cleanedRoot = cleanedRoot.substring(1)
      }

      if (cleanedRoot.endsWith("/")) {
        cleanedRoot = cleanedRoot.substring(0, cleanedRoot.length - 1)
      }

      return cleanedRoot
    }

  val rootNxJsonPath: String
    get() {
      if (this.configuredRootPath == "/") {
        return "nx.json"
      }

      return "${this.configuredRootPath}/nx.json"
    }

  private val nxCliVersion: SemVer?
    get() = NodeInstalledPackageFinder(this.project, this.projectRoot).findInstalledPackage("@nrwl/cli")?.version

  private val isNx11OrAbove: Boolean
    get() = this.nxCliVersion != null && this.nxCliVersion!!.major >= 11 

   private val isNx16OrAbove: Boolean
    get() = this.nxCliVersion != null && this.nxCliVersion!!.major >= 16 

val defaultCustomSchematicsLocation: String
    get() = if (this.isNx16OrAbove) {
        "/tools/workspace-plugin/src/generators"
    } else if (this.isNx10OrAbove) {
        "/tools/schematics-nx10"
    } else {
        "/tools/schematics"
    }

  /**
   * root directory at which your nx monorepo resides within open project
   */
  val defaultRootDirectory: String
    get() = "/"

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

  /**
   * whether this is toggled as pnpm project
   */
  val isPnpm: Boolean
    get() {
      val settings = PluginProjectSettingsState.getInstance(this.project)
      return settings.isPnpm
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
    return readFile.readJsonFromFileUrl(this.rootNxJsonPath)
  }

  private fun readAngularJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("angular.json")
  }

  private fun readWorkspaceJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("workspace.json")
  }

  private fun getProjects(): Array<String> {
    if (this.isValidNxProject) {
      val json = this.workspaceJson ?: return emptyArray()

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
