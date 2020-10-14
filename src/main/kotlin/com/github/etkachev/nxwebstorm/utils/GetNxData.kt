package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.openapi.project.Project

class GetNxData(private val project: Project) {
  fun getProjects(): List<String> {
    val json = this.readNxJson() ?: return emptyList()

    val projects = json.getAsJsonObject("projects").keySet()
    return projects.toList()
  }

  fun isValidNxProject(): Boolean {
    return readNxJson() != null
  }

  private fun readNxJson(): JsonObject? {
    return ReadFile(project).readJsonFromFileUrl("nx.json")
  }
}
