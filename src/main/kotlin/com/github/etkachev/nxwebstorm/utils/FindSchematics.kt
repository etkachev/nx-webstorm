package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.intellij.openapi.project.Project

class FindSchematics(project: Project, private val externalLibs: Array<String>) {
  var jsonFileReader = ReadFile(project)
  var schematicPropName = "schematics"
  var nodeModulesFolder = "node_modules"

  private fun getSchematicsFromDirectory(directory: String): Map<String, SchematicInfo>? {
    val packageFile = jsonFileReader.findVirtualFile("$nodeModulesFolder/$directory/package.json") ?: return null
    val packageFileJson = jsonFileReader.readJsonFromFile(packageFile) ?: return null
    val packageName = (if (packageFileJson.has("name")) packageFileJson["name"].asString else null) ?: return null
    val schematicProp =
      (if (packageFileJson.has(schematicPropName)) packageFileJson[schematicPropName].asString else null)
        ?: return null
    val schematicCollection = jsonFileReader.findVirtualFile(schematicProp, packageFile.parent) ?: return null
    val collectionJson = jsonFileReader.readJsonFromFile(schematicCollection) ?: return null
    val schematicsOptions =
      (if (collectionJson.has(schematicPropName)) collectionJson[schematicPropName].asJsonObject else null)
        ?: return null
    val schematicEntries =
      schematicsOptions.entrySet().toTypedArray().fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
        val value = e.value.asJsonObject
        if (value.has("hidden") && value["hidden"].asBoolean) {
          return@fold acc
        }

        val schemaFileLocation = (if (value.has("schema")) value["schema"].asString else null) ?: return@fold acc
        val schemaFile =
          jsonFileReader.findVirtualFile(schemaFileLocation, schematicCollection.parent) ?: return@fold acc
        val splitFile = schemaFile.path.split(directory)
        val relativePath = if (splitFile.count() == 2) splitFile[1] else null ?: return@fold acc
        val fileLocation = "$nodeModulesFolder/${directory}${relativePath}"
        val id = generateUniqueSchematicKey(packageName, e.key)
        val description = if (value.has("description")) value["description"].asString else null
        acc[id] = SchematicInfo(fileLocation, description)
        return@fold acc
      })

    return schematicEntries.toMap()
  }

  fun findSchematics(): Map<String, SchematicInfo> {
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
      ).findSchematics()
    return flattenMultipleMaps(customSchematics, more)
  }
}
