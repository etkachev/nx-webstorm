package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

class GetNxData(private val project: Project) {
  var defaultToolsSchematicDir = "/tools/schematics"
  var configToolsSchematicDir: String
  private val cleanedUpConfigToolsSchematicDir: String
    get() = if (configToolsSchematicDir.startsWith("/")) configToolsSchematicDir else "/$configToolsSchematicDir"
  private val toolsSchematicDir: String
    get() = if (configToolsSchematicDir.isBlank()) defaultToolsSchematicDir else cleanedUpConfigToolsSchematicDir
  private val splitSchematicDir: Array<String>
    get() = toolsSchematicDir.split("/").mapNotNull { s -> if (s.isBlank()) null else s }.toTypedArray()

  init {
    val settings: PluginSettingsState = PluginSettingsState.instance
    configToolsSchematicDir = settings.customSchematicLocation
  }

  fun getProjects(): List<String> {
    val json = this.readNxJson() ?: return emptyList()

    val projects = json.getAsJsonObject("projects").keySet()
    return projects.toList()
  }

  fun getCustomSchematics(): Map<String, SchematicInfo> {
    val root = ProjectRootManager.getInstance(project).contentRoots[0]
    val rootPsiDirectory = PsiManager.getInstance(project).findDirectory(root)
    val schematics = findPsiDirectoryBySplitFolders(splitSchematicDir, rootPsiDirectory) ?: return emptyMap()
    val files = FilenameIndex.getFilesByName(
      project, "schema.json",
      GlobalSearchScopes.directoriesScope(project, true, schematics.virtualFile)
    )
    return files.mapNotNull { file -> getIdsFromSchema(file) }.toMap()
  }

  private fun findPsiDirectoryBySplitFolders(
    dir: Array<String>,
    rootPsiDirectory: PsiDirectory?,
    dirIndex: Int = 0,
    checkedDirectory: PsiDirectory? = null
  ): PsiDirectory? {
    if (dir.count() == 0 || rootPsiDirectory == null) {
      return null
    }

    // if reached end of array, return currentDirectory
    if (dirIndex == dir.count()) {
      return checkedDirectory
    }

    val currentPsiDir = (if (dirIndex == 0) rootPsiDirectory else checkedDirectory) ?: return null

    val currentDir = dir[dirIndex]
    val subPsiDir = currentPsiDir.findSubdirectory(currentDir) ?: return null
    return findPsiDirectoryBySplitFolders(dir, rootPsiDirectory, dirIndex + 1, subPsiDir)
  }

  private fun getIdsFromSchema(file: PsiFile): Pair<String, SchematicInfo>? {
    val json = JsonParser.parseString(file.text).asJsonObject ?: return null
    if (!json.has("id")) {
      return null
    }
    val id = json.get("id").asString
    // TODO need to find safer way to build file location.
    val fileLocation = "$toolsSchematicDir/$id/schema.json"
    val info = SchematicInfo(fileLocation)
    val uniqueId = generateUniqueSchematicKey("workspace-schematic", id)
    return uniqueId to info
  }

  fun isValidNxProject(): Boolean {
    return readNxJson() != null
  }

  private fun readNxJson(): JsonObject? {
    return ReadFile(project).readJsonFromFileUrl("nx.json")
  }
}
