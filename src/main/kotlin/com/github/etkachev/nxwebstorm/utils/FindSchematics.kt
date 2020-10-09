package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

data class CollectionInfo(val json: JsonObject, val file: VirtualFile)
class FindSchematics(private val project: Project, private val externalLibs: Array<String>) {
  var jsonFileReader = ReadFile(project)
  var packageJsonHelper = PackageJsonHelper(project)
  var schematicPropName = "schematics"
  var nodeModulesFolder = "node_modules"

  fun findByExternalLibs(): Map<String, SchematicInfo> {
    val schematicMaps = externalLibs.mapNotNull { dir -> getSchematicsFromNodeModulesDirectory(dir) }
    return foldListOfMaps(schematicMaps.toTypedArray())
  }

  fun scanAllForExternalSchematics(): Map<String, SchematicInfo> {
    val psiDir = getRootPsiDirectory(project) ?: return emptyMap()

    val nodeModules = psiDir.findSubdirectory(nodeModulesFolder) ?: return emptyMap()
    val splitProjectRootPath = psiDir.virtualFile.path.split("/")
    val projectRootFolder = splitProjectRootPath[splitProjectRootPath.count() - 1]
    val packageJsonFiles = FilenameIndex.getFilesByName(
      project,
      "package.json",
      GlobalSearchScopes.directoriesScope(project, true, nodeModules.virtualFile)
    ).mapNotNull { f ->
      val (packageName, packageFileJson, packageFile) = packageJsonHelper.getPackageFileByVirtualFile(f.virtualFile)
        ?: return@mapNotNull null
      val (schematicsOptions, schematicCollection) = getSchematicOptions(packageFileJson, packageFile)
        ?: return@mapNotNull null
      val splitFullFileLocation = f.parent!!.virtualFile.path.split("$projectRootFolder/$nodeModulesFolder/")
      val nodeModulesFileLocation = splitFullFileLocation[1]
      val packageFileLocation = "$nodeModulesFolder/$nodeModulesFileLocation"
      val results = getSchematicEntries(
        schematicsOptions,
        nodeModulesFileLocation,
        schematicCollection,
        packageName,
        fileLocationMapper = { path -> "$packageFileLocation$path" })
      return@mapNotNull if (results.isNotEmpty()) results else null
    }.toTypedArray()
    return foldListOfMaps(packageJsonFiles)
  }

  private fun getSchematicEntries(
    schematicOptions: JsonObject,
    directory: String,
    schematicCollection: VirtualFile,
    packageName: String,
    fileLocationMapper: (path: String) -> String
  ): Map<String, SchematicInfo> {
    return schematicOptions.entrySet().toTypedArray().fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
      val value = e.value.asJsonObject
      if (value.has("hidden") && value["hidden"].asBoolean) {
        return@fold acc
      }

      val relativePath = getRelativePath(directory, value, schematicCollection) ?: return@fold acc
      val fileLocation = fileLocationMapper(relativePath)
      val id = generateUniqueSchematicKey(packageName, e.key)
      val description = if (value.has("description")) value["description"].asString else null
      acc[id] = SchematicInfo(fileLocation, description)
      return@fold acc
    }).toMap()
  }

  private fun getSchematicsFromNodeModulesDirectory(directory: String): Map<String, SchematicInfo>? {
    val packageJson = packageJsonHelper.getPackageFileInfo("$nodeModulesFolder/$directory") ?: return null
    val (packageName, packageFileJson, packageFile) = packageJson
    val (schematicsOptions, schematicCollection) = getSchematicOptions(packageFileJson, packageFile) ?: return null
    return getSchematicEntries(
      schematicsOptions,
      directory,
      schematicCollection,
      packageName,
      fileLocationMapper = { path -> "$nodeModulesFolder/$directory$path" })
  }

  private fun getSchematicOptions(packageFileJson: JsonObject, packageFile: VirtualFile): CollectionInfo? {
    val schematicProp =
      (if (packageFileJson.has(schematicPropName)) packageFileJson[schematicPropName].asString else null)
        ?: return null
    val schematicCollection = jsonFileReader.findVirtualFile(schematicProp, packageFile.parent) ?: return null
    val collectionJson = jsonFileReader.readJsonFromFile(schematicCollection) ?: return null
    val schematicsOptions =
      (if (collectionJson.has(schematicPropName)) collectionJson[schematicPropName].asJsonObject else null)
        ?: return null
    return CollectionInfo(schematicsOptions, schematicCollection)
  }

  private fun getRelativePath(directory: String, value: JsonObject, schematicCollection: VirtualFile): String? {
    val schemaFileLocation = (if (value.has("schema")) value["schema"].asString else null) ?: return null
    val schemaFile =
      jsonFileReader.findVirtualFile(schemaFileLocation, schematicCollection.parent) ?: return null
    val splitFile = schemaFile.path.split(directory)
    return if (splitFile.count() == 2) splitFile[1] else null ?: return null
  }
}

class FindAllSchematics(private val project: Project) {
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

  fun findAll(): Map<String, SchematicInfo> {
    val customSchematics = getCustomSchematics()
    val settings: PluginSettingsState = PluginSettingsState.instance
    if (settings.scanExplicitLibs) {
      val others = settings.externalLibs.split(",").mapNotNull { value ->
        val trimmed = value.trim()
        val final = if (trimmed.isEmpty()) null else trimmed
        final
      }
      val more =
        FindSchematics(
          project,
          others.toTypedArray()
        ).findByExternalLibs()
      return flattenMultipleMaps(customSchematics, more)
    } else {
      val allScanned = FindSchematics(project, emptyArray()).scanAllForExternalSchematics()
      return flattenMultipleMaps(customSchematics, allScanned)
    }
  }

  private fun getCustomSchematics(): Map<String, SchematicInfo> {
    val rootPsiDirectory = getRootPsiDirectory(project)
    val schematics = findPsiDirectoryBySplitFolders(splitSchematicDir, rootPsiDirectory) ?: return emptyMap()
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
    val fileLocation = "$toolsSchematicDir/$id/schema.json"
    val info = SchematicInfo(fileLocation)
    val uniqueId = generateUniqueSchematicKey("workspace-schematic", id)
    return uniqueId to info
  }
}
