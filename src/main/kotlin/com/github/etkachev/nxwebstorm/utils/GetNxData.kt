package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

class GetNxData {
    fun getProjects(project: Project): List<String> {
        val json = this.readNxJson(project)

        val projects = json.getAsJsonObject("projects").keySet()
        return projects.toList()
    }

    fun getCustomSchematics(project: Project): Map<String, String> {
        val root = ProjectRootManager.getInstance(project).contentRoots[0]
        val psiDir = PsiManager.getInstance(project).findDirectory(root) ?: return emptyMap()
        val schematics = psiDir.findSubdirectory("tools")!!.findSubdirectory("schematics") ?: return emptyMap()
        val files = FilenameIndex.getFilesByName(
            project, "schema.json",
            GlobalSearchScopes.directoriesScope(project, true, schematics.virtualFile)
        )
        return files.mapNotNull { file -> getIdsFromSchema(file) }.toMap()
    }

    private fun getIdsFromSchema(file: PsiFile): Pair<String, String>? {
        val json = JsonParser.parseString(file.text).asJsonObject ?: return null
        if (!json.has("id")) {
            return null
        }
        val id = json.get("id").asString
        val fileLocation = "/tools/schematics/$id/schema.json"
        return id to fileLocation
    }

    private fun readNxJson(project: Project): JsonObject {
        return try {
            ReadJsonFile().fromFileUrl(project, "nx.json")
        } catch (e: NoSuchElementException) {
            null
        }
            ?: throw NoSuchElementException("Could not find nx.json file")
    }
}
