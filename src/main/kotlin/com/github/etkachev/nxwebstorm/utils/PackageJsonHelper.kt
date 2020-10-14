package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class PackageJsonInfo(val packageName: String, val json: JsonObject, val file: VirtualFile)

class PackageJsonHelper(project: Project) {
  var jsonFileReader = ReadFile(project)

  fun getPackageFileInfo(directory: String): PackageJsonInfo? {
    val packageFile = jsonFileReader.findVirtualFile("$directory/package.json") ?: return null
    return getPackageFileByVirtualFile(packageFile)
  }

  fun getPackageFileByVirtualFile(file: VirtualFile): PackageJsonInfo? {
    val packageFileJson = jsonFileReader.readJsonFromFile(file) ?: return null
    val packageName = (if (packageFileJson.has("name")) packageFileJson["name"].asString else null) ?: return null
    return PackageJsonInfo(packageName, packageFileJson, file)
  }
}
