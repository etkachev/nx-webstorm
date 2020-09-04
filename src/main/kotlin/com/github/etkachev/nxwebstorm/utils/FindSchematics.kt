package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class CollectionInfo(val json: JsonObject, val file: VirtualFile)
data class PackageJsonInfo(val packageName: String, val json: JsonObject, val file: VirtualFile)
class FindSchematics(project: Project, private val externalLibs: Array<String>) {
  var jsonFileReader = ReadFile(project)
  var schematicPropName = "schematics"
  var nodeModulesFolder = "node_modules"

  private fun getPackageFileInfo(directory: String): PackageJsonInfo? {
    val packageFile = jsonFileReader.findVirtualFile("$nodeModulesFolder/$directory/package.json") ?: return null
    val packageFileJson = jsonFileReader.readJsonFromFile(packageFile) ?: return null
    val packageName = (if (packageFileJson.has("name")) packageFileJson["name"].asString else null) ?: return null
    return PackageJsonInfo(packageName, packageFileJson, packageFile)
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
    return externalLibs.mapNotNull { dir -> getSchematicsFromDirectory(dir) }
      .fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
        for (key in e.keys) {
          val info = e[key] ?: continue
          acc[key] = info
        }
        return@fold acc
      }).toMap()
  }
}

class FindAllSchematics(private val project: Project) {
  fun findAll(): Map<String, SchematicInfo> {
    val customSchematics = GetNxData().getCustomSchematics(project)
    val more =
      FindSchematics(
        project,
        arrayOf(
          "@nrwl/angular",
          "@nrwl/nest",
          "@nrwl/node",
          "@nrwl/storybook",
          "@nrwl/workspace",
          "@schematics/angular",
          "@nestjs/schematics",
          "@ngrx/schematics"
        )
      ).findByExternalLibs()
    return flattenMultipleMaps(customSchematics, more)
  }
}
