package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.settings.PluginProjectSettingsState
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

/**
 * @property relativePath path relative to project base
 */
data class CollectionInfo(val json: JsonObject, val file: VirtualFile, val relativePath: String)

/**
 * util class for finding and loading schematics (both provided and custom schematics).
 */
class FindAllSchematics(private val project: Project) {
  private val projectSettings = PluginProjectSettingsState.getInstance(project)
  private val nxService = MyProjectService.getInstance(project)
  private val defaultToolsSchematicDir = this.nxService.defaultCustomSchematicsLocation
  private val configToolsSchematicDir: String
    get() {
      return if (projectSettings.customSchematicsLocation.isNotBlank()) {
        projectSettings.customSchematicsLocation
      } else {
        this.defaultToolsSchematicDir
      }
    }
  private val jsonFileReader = ReadFile.getInstance(project)
  private val packageJsonHelper = PackageJsonHelper(project)
  private val schematicPropName = "schematics"
  private val nodeModulesFolder: String
    get() {
      val rootDir = this.nxService.configuredRootPath
      return if (rootDir == "/") {
        "node_modules"
      } else {
        "$rootDir/node_modules"
      }
    }

  private val cleanedUpConfigToolsSchematicDir: String
    get() = if (configToolsSchematicDir.startsWith("/")) configToolsSchematicDir else "/$configToolsSchematicDir"
  private val toolsSchematicDir: String
    get() = if (configToolsSchematicDir.isBlank()) defaultToolsSchematicDir else cleanedUpConfigToolsSchematicDir
  private val splitSchematicDir: Array<String>
    get() = toolsSchematicDir.split("/").mapNotNull { s -> if (s.isBlank()) null else s }.toTypedArray()

  /**
   * Fetch both custom schematics and external schematics.
   * Depending on settings for plugin, will either search explicit
   * libs for external schematics or scan full node_modules for external schematics.
   */
  fun findAll(): Map<String, SchematicInfo> {
    val customSchematics = getCustomSchematics()
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(project)
    return if (settings.scanExplicitLibs) {
      val others = settings.externalLibs.mapNotNull { value ->
        val trimmed = value.trim()
        val final = if (trimmed.isEmpty()) null else trimmed
        final
      }
      val more =
        this.findByExternalLibs(others.toTypedArray())
      flattenMultipleMaps(customSchematics, more)
    } else {
      val allScanned = this.scanAllForExternalSchematics()
      flattenMultipleMaps(customSchematics, allScanned)
    }
  }

  /**
   * Fetch custom schematics.
   * - If nx project search just schema.json files within `/tools/schematics` (or overload folder).
   * - Otherwise if regular angular project,
   * search custom schematics from package.json file within expected schematics folder of project.
   */
  private fun getCustomSchematics(): Map<String, SchematicInfo> {
    val rootPsiDirectory = getRootPsiDirectory(project)
    val projectBase = project.basePath ?: return emptyMap()

    if (nxService.isValidNxProject) {
      val schematics = findPsiDirectoryBySplitFolders(splitSchematicDir, rootPsiDirectory) ?: return emptyMap()
      val files = FilenameIndex.getFilesByName(
        project,
        "schema.json",
        GlobalSearchScopes.directoriesScope(project, true, schematics.virtualFile)
      )
      return files.mapNotNull { file -> getIdsFromSchema(file, projectBase) }.toMap()
    } else if (nxService.isAngularProject) {
      val angularSchematics = this.getAngularCustomSchematics(this.toolsSchematicDir)
      return angularSchematics ?: emptyMap()
    }
    return emptyMap()
  }

  /**
   * for custom nx schematics, generate custom schematic id with additional schematic info.
   */
  private fun getIdsFromSchema(file: PsiFile, projectBase: String): Pair<String, SchematicInfo>? {
    val json = JsonParser.parseString(file.text).asJsonObject ?: return null
    if (!json.has("id")) {
      return null
    }
    val id = json.get("id").asString
    val splitDir = file.virtualFile.path.split(projectBase)
    val fileLocation = (if (splitDir.count() != 2) null else splitDir[1]) ?: return null
    val info = SchematicInfo(fileLocation)
    val uniqueId = generateUniqueSchematicKey("workspace-schematic", id, SchematicTypeEnum.CUSTOM_NX)
    return uniqueId to info
  }

  /**
   * based on schematic options from collection file, generate full info of schematic with generated ids.
   */
  private fun getSchematicEntries(
    schematicOptions: JsonObject,
    schematicCollection: VirtualFile,
    packageName: String,
    schematicType: SchematicTypeEnum,
    collectionPath: String
  ): Map<String, SchematicInfo> {
    return schematicOptions.entrySet().toTypedArray().fold(
      mutableMapOf<String, SchematicInfo>(),
      { acc, e ->
        val value = e.value.asJsonObject
        if (value.has("hidden") && value["hidden"].asBoolean) {
          return@fold acc
        }

        val fileLocation = getRelativePath(value, schematicCollection) ?: return@fold acc
        val id = generateUniqueSchematicKey(packageName, e.key, schematicType)
        val description = if (value.has("description")) value["description"].asString else null
        acc[id] = SchematicInfo(fileLocation, description, collectionPath)
        return@fold acc
      }
    ).toMap()
  }

  /**
   * for non-nx angular project, search for custom schematics
   * via package.json file within expected custom schematics folder.
   */
  private fun getAngularCustomSchematics(directory: String): Map<String, SchematicInfo>? {
    val packageJson = packageJsonHelper.getPackageFileInfo(directory) ?: return null
    val (packageName, packageFileJson, packageFile) = packageJson
    val (schematicOptions, schematicCollection, collectionPath) = getSchematicOptions(packageFileJson, packageFile)
      ?: return null
    return getSchematicEntries(
      schematicOptions,
      schematicCollection,
      packageName,
      SchematicTypeEnum.CUSTOM_ANGULAR,
      collectionPath
    )
  }

  /**
   * find schematics info for expected node_modules directory.
   * Searching for provided schematics.
   */
  private fun getSchematicsFromNodeModulesDirectory(directory: String): Map<String, SchematicInfo>? {
    val packageJson = packageJsonHelper.getPackageFileInfo("$nodeModulesFolder/$directory") ?: return null
    val (packageName, packageFileJson, packageFile) = packageJson
    val (schematicsOptions, schematicCollection, collectionPath) = getSchematicOptions(packageFileJson, packageFile)
      ?: return null
    return getSchematicEntries(
      schematicsOptions,
      schematicCollection,
      packageName,
      SchematicTypeEnum.PROVIDED,
      collectionPath
    )
  }

  /**
   * get collection info on schematic options for passed in package.json file.
   */
  private fun getSchematicOptions(packageFileJson: JsonObject, packageFile: VirtualFile): CollectionInfo? {
    val schematicProp =
      (if (packageFileJson.has(schematicPropName)) packageFileJson[schematicPropName].asString else null)
        ?: return null
    val schematicCollection = jsonFileReader.findVirtualFile(schematicProp, packageFile.parent) ?: return null
    val collectionJson = jsonFileReader.readJsonFromFile(schematicCollection) ?: return null
    val projPath = project.basePath ?: return null
    val relativePathSplit = schematicCollection.path.split(projPath)
    if (relativePathSplit.count() < 2) {
      return null
    }
    val relativePath = relativePathSplit[1]
    val schematicsOptions =
      (if (collectionJson.has(schematicPropName)) collectionJson[schematicPropName].asJsonObject else null)
        ?: return null
    return CollectionInfo(schematicsOptions, schematicCollection, relativePath)
  }

  /**
   * gets relative path (to project) of schema file based on collection.json file.
   */
  private fun getRelativePath(value: JsonObject, schematicCollection: VirtualFile): String? {
    val schemaFileLocation = (if (value.has("schema")) value["schema"].asString else null) ?: return null
    val schemaFile =
      jsonFileReader.findVirtualFile(schemaFileLocation, schematicCollection.parent) ?: return null
    val splitFile = schemaFile.path.split(this.project.basePath ?: "")
    return if (splitFile.count() == 2) splitFile[1] else null
  }

  /**
   * get schematic info based on external libs that are explicit to scan.
   */
  private fun findByExternalLibs(externalLibs: Array<String>): Map<String, SchematicInfo> {
    val schematicMaps = externalLibs.mapNotNull { dir -> getSchematicsFromNodeModulesDirectory(dir) }
    return foldListOfMaps(schematicMaps.toTypedArray())
  }

  /**
   * can all possible external schematics within node_modules folder
   */
  private fun scanAllForExternalSchematics(): Map<String, SchematicInfo> {
    val psiDir = getRootPsiDirectory(project) ?: return emptyMap()

    val nodeModules = psiDir.findSubdirectory(nodeModulesFolder) ?: return emptyMap()
    val packageJsonFiles = FilenameIndex.getFilesByName(
      project,
      "package.json",
      GlobalSearchScopes.directoriesScope(project, true, nodeModules.virtualFile)
    ).mapNotNull { f ->
      val (packageName, packageFileJson, packageFile) = packageJsonHelper.getPackageFileByVirtualFile(f.virtualFile)
        ?: return@mapNotNull null
      val (schematicsOptions, schematicCollection, collectionPath) = getSchematicOptions(packageFileJson, packageFile)
        ?: return@mapNotNull null
      val results = getSchematicEntries(
        schematicsOptions,
        schematicCollection,
        packageName,
        SchematicTypeEnum.PROVIDED,
        collectionPath
      )
      return@mapNotNull if (results.isNotEmpty()) results else null
    }.toTypedArray()
    return foldListOfMaps(packageJsonFiles)
  }
}
