package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.openapi.project.Project

class GetNxData(private val project: Project) {
  fun getProjects(): Array<String> {
    val json = this.readNxJson() ?: return emptyArray()

    val projects = json.getAsJsonObject("projects").keySet()
    return projects.toTypedArray()
  }

  fun isValidNxProject(): Boolean {
    return readNxJson() != null
  }

  private fun readNxJson(): JsonObject? {
    return ReadFile(project).readJsonFromFileUrl("nx.json")
  }
}
