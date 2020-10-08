package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.settings.PluginSettingsState
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

data class CollectionInfo(val json: JsonObject, val file: VirtualFile)
data class PackageJsonInfo(val packageName: String, val json: JsonObject, val file: VirtualFile)
class FindSchematics(private val project: Project, private val externalLibs: Array<String>) {
  var jsonFileReader = ReadFile(project)
  var schematicPropName = "schematics"
  var nodeModulesFolder = "node_modules"

  private fun getPackageFileInfo(directory: String): PackageJsonInfo? {
    val packageFile = jsonFileReader.findVirtualFile("$nodeModulesFolder/$directory/package.json") ?: return null
    return getPackageFileByVirtualFile(packageFile)
  }

  private fun getPackageFileByVirtualFile(file: VirtualFile): PackageJsonInfo? {
    val packageFileJson = jsonFileReader.readJsonFromFile(file) ?: return null
    val packageName = (if (packageFileJson.has("name")) packageFileJson["name"].asString else null) ?: return null
    return PackageJsonInfo(packageName, packageFileJson, file)
  }

  private fun findAllPackageJsonFiles(): Map<String, SchematicInfo> {
    val root = ProjectRootManager.getInstance(project).contentRoots[0]
    val psiDir = PsiManager.getInstance(project).findDirectory(root) ?: return emptyMap()

    val nodeModules = psiDir.findSubdirectory(nodeModulesFolder) ?: return emptyMap()
    val splitProjectRootPath = psiDir.virtualFile.path.split("/")
    val projectRootFolder = splitProjectRootPath[splitProjectRootPath.count() - 1]
    val packageJsonFiles = FilenameIndex.getFilesByName(
      project,
      "package.json",
      GlobalSearchScopes.directoriesScope(project, true, nodeModules.virtualFile)
    ).mapNotNull { f ->
      val (packageName, packageFileJson, packageFile) = getPackageFileByVirtualFile(f.virtualFile)
        ?: return@mapNotNull null
      val (schematicsOptions, schematicCollection) = getSchematicOptions(packageFileJson, packageFile)
        ?: return@mapNotNull null
      val splitFullFileLocation = f.parent!!.virtualFile.path.split("$projectRootFolder/$nodeModulesFolder/")
      val nodeModulesFileLocation = splitFullFileLocation[1]
      val packageFileLocation = "$nodeModulesFolder/$nodeModulesFileLocation"
      val schematicEntries =
        schematicsOptions.entrySet().toTypedArray().fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
          val value = e.value.asJsonObject
          if (value.has("hidden") && value["hidden"].asBoolean) {
            return@fold acc
          }

          val relativePath = getRelativePath(nodeModulesFileLocation, value, schematicCollection) ?: return@fold acc
          val schematicsFileLocation = "$packageFileLocation$relativePath"
          val id = generateUniqueSchematicKey(packageName, e.key)
          val description = if (value.has("description")) value["description"].asString else null
          acc[id] = SchematicInfo(schematicsFileLocation, description)
          return@fold acc
        })
      val results = schematicEntries.toMap()
      return@mapNotNull if (results.isNotEmpty()) results else null
    }.toTypedArray()
    return foldListOfSchematicMaps(packageJsonFiles)
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

  private fun getSchematicsFromDirectory(directory: String): Map<String, SchematicInfo>? {
    val packageJson = getPackageFileInfo(directory) ?: return null
    val (packageName, packageFileJson, packageFile) = packageJson
    val (schematicsOptions, schematicCollection) = getSchematicOptions(packageFileJson, packageFile) ?: return null
    val schematicEntries =
      schematicsOptions.entrySet().toTypedArray().fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
        val value = e.value.asJsonObject
        if (value.has("hidden") && value["hidden"].asBoolean) {
          return@fold acc
        }
        val relativePath = getRelativePath(directory, value, schematicCollection) ?: return@fold acc
        val fileLocation = "$nodeModulesFolder/$directory$relativePath"
        val id = generateUniqueSchematicKey(packageName, e.key)
        val description = if (value.has("description")) value["description"].asString else null
        acc[id] = SchematicInfo(fileLocation, description)
        return@fold acc
      })

    return schematicEntries.toMap()
  }

  fun findByExternalLibs(): Map<String, SchematicInfo> {
    val schematicMaps = externalLibs.mapNotNull { dir -> getSchematicsFromDirectory(dir) }
    return foldListOfSchematicMaps(schematicMaps.toTypedArray())
  }

  private fun foldListOfSchematicMaps(schematicMaps: Array<Map<String, SchematicInfo>>): Map<String, SchematicInfo> {
    return schematicMaps.fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
      for (key in e.keys) {
        val info = e[key] ?: continue
        acc[key] = info
      }
      return@fold acc
    }).toMap()
  }

  fun scanAllForExternalSchematics(): Map<String, SchematicInfo> {
    return findAllPackageJsonFiles()
  }
}

class FindAllSchematics(private val project: Project) {
  fun findAll(): Map<String, SchematicInfo> {
    val customSchematics = GetNxData(project).getCustomSchematics()
    val settings: PluginSettingsState = PluginSettingsState.instance
    val findExplicitSchematics = settings.scanExplicitLibs
    if (findExplicitSchematics) {
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
}
