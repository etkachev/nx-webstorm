package com.github.etkachev.nxwebstorm.services

import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.intellij.openapi.project.Project
import com.github.etkachev.nxwebstorm.utils.ReadFile
import com.google.gson.JsonObject

class MyProjectService(project: Project) {
  private var readFile = ReadFile(project)
  val nxJson: JsonObject?
    get() = readNxJson()
  val angularJson: JsonObject?
    get() = readAngularJson()

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

  private fun readNxJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("nx.json")
  }

  private fun readAngularJson(): JsonObject? {
    return readFile.readJsonFromFileUrl("angular.json")
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
}
