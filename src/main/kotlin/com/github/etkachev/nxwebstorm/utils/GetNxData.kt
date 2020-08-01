package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.openapi.project.Project

class GetNxData {
    fun getProjects(project: Project): List<String> {
        val json = this.readNxJson(project)

        val projects = json.getAsJsonObject("projects").keySet()
        return projects.toList()
    }

    private fun readNxJson(project: Project): JsonObject {
        return try {
            ReadJsonFile().fromFileUrl(project, "nx.json")
        } catch (e: NoSuchElementException) { null }
                ?: throw NoSuchElementException("Could not find nx.json file")
    }
}