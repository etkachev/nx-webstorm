package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jdom.Document
import java.io.IOException

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

  fun saveXml(document: Document, filePath: String): Document? {
    return try {
      ApplicationManager.getApplication().runWriteAction(ComputeWriteXmlFile(document, filePath))
    } catch (e: IOException) {
      return null
    }
  }

  companion object {
    fun getInstance(project: Project): ReadFile {
      return project.getService(ReadFile::class.java)
    }
  }
}
