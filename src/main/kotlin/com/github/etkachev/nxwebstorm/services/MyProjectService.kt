package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.intellij.openapi.project.Project
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.google.gson.JsonObject

class MyProjectService(project: Project) {
  private var _projectList: Array<String>? = null
  private var readFile = ReadFile(project)
  var nxJson = readNxJson()
  var angularJson = readAngularJson()

  /**
   * full list of projects/libraries within current project.
   */
  val projectList: Array<String>
    get() {
      if (this._projectList != null) {
        return this._projectList!!
      }

      this._projectList = this.getProjects()
      return this._projectList!!
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

  private fun readNxJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("nx.json")
  }

  private fun readAngularJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("angular.json")
  }

  private fun getProjects(): Array<String> {
    val json = this.nxJson ?: return emptyArray()

    val projects = json.getAsJsonObject("projects").keySet()
    return projects.toTypedArray()
  }
}
