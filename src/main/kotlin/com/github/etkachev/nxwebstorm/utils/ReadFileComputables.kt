package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jdom.Document
import java.io.File
import java.io.IOException
import java.nio.file.WatchService

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

class ComputeWriteXmlFile(
  private val document: Document,
  private val filePath: String
) : ThrowableComputable<Document, IOException> {
  override fun compute(): Document {
    try {
      JDOMUtil.writeDocument(document, filePath, "\n")
    } catch (e: IOException) {
      throw IOException("Could not save xml file")
    }
    return JDOMUtil.loadDocument(File(filePath))
  }

  // private fun isFileReady(): Boolean {
  //   try {
  //     val file = JDOMUtil.loadDocument(File(filePath))
  //     return true
  //   } catch (e: Exception) {
  //     return false
  //   }
  // }
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
