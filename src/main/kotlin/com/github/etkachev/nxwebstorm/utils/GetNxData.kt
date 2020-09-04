package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
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
    val json = this.readNxJson(project) ?: return emptyList()

    val projects = json.getAsJsonObject("projects").keySet()
    return projects.toList()
  }

  fun getCustomSchematics(project: Project): Map<String, SchematicInfo> {
    val root = ProjectRootManager.getInstance(project).contentRoots[0]
    val psiDir = PsiManager.getInstance(project).findDirectory(root) ?: return emptyMap()
    val schematics = psiDir.findSubdirectory("tools")!!.findSubdirectory("schematics") ?: return emptyMap()
    val files = FilenameIndex.getFilesByName(
      project, "schema.json",
      GlobalSearchScopes.directoriesScope(project, true, schematics.virtualFile)
    )
    return files.mapNotNull { file -> getIdsFromSchema(file) }.toMap()
  }

  private fun getIdsFromSchema(file: PsiFile): Pair<String, SchematicInfo>? {
    val json = JsonParser.parseString(file.text).asJsonObject ?: return null
    if (!json.has("id")) {
      return null
    }
    val id = json.get("id").asString
    // TODO need to find safer way to build file location.
    val fileLocation = "/tools/schematics/$id/schema.json"
    val info = SchematicInfo(fileLocation)
    val uniqueId = generateUniqueSchematicKey("workspace-schematic", id)
    return uniqueId to info
  }

  fun isValidNxProject(project: Project): Boolean {
    return readNxJson(project) != null
  }

  private fun readNxJson(project: Project): JsonObject? {
    return ReadFile(project).readJsonFromFileUrl("nx.json")
  }
}
