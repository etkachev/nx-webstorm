package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class ComputeReadJsonFile(
  private val root: VirtualFile,
  private val psi: PsiManager,
  private val filePath: String
) :
  ThrowableComputable<JsonObject, NoSuchElementException> {
  override fun compute(): JsonObject {
    val file = root.findFileByRelativePath(filePath)
      ?: throw NoSuchElementException("Could not find file for $filePath")
    val fileContents = psi.findFile(file)
      ?: throw NoSuchElementException("Could not find file for $filePath")
    val json = JsonParser.parseString(fileContents.text)
    return json.asJsonObject
  }
}

class ComputeReadJsonFileFromVirtualFile(
  private val file: VirtualFile,
  private val psi: PsiManager
) : ThrowableComputable<JsonObject, NoSuchElementException> {
  override fun compute(): JsonObject {
    val fileContents = psi.findFile(file) ?: throw NoSuchElementException("Could not find file for ${file.url}")
    val json = JsonParser.parseString(fileContents.text)
    return json.asJsonObject
  }
}

class ComputeReadVirtualFile(
  private val root: VirtualFile,
  private val filePath: String
) : ThrowableComputable<VirtualFile, NoSuchElementException> {
  override fun compute(): VirtualFile {
    return root.findFileByRelativePath(filePath)
      ?: throw NoSuchElementException("Could not find file or directory for $filePath")
  }
}

class ReadFile(project: Project) {
  var root = ProjectRootManager.getInstance(project).contentRoots[0]
  var psi = PsiManager.getInstance(project)
  fun readJsonFromFileUrl(filePath: String): JsonObject? {
    return try {
      ApplicationManager.getApplication().runReadAction(ComputeReadJsonFile(root, psi, filePath))
    } catch (e: NoSuchElementException) {
      return null
    }
  }

  fun readJsonFromFile(file: VirtualFile): JsonObject? {
    return try {
      ApplicationManager.getApplication().runReadAction(ComputeReadJsonFileFromVirtualFile(file, psi))
    } catch (e: NoSuchElementException) {
      return null
    }
  }

  fun findVirtualFile(filePath: String, startLocation: VirtualFile = root): VirtualFile? {
    return try {
      ApplicationManager.getApplication().runReadAction(ComputeReadVirtualFile(startLocation, filePath))
    } catch (e: NoSuchElementException) {
      return null
    }
  }
}
