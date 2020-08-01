package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiManager

class ReadJsonFile {
    fun fromFileUrl(project: Project, filePath: String): JsonObject {
        val file = ProjectRootManager.getInstance(project).contentRoots[0].findFileByRelativePath(filePath)
                ?: throw NoSuchElementException("Could not find file for $filePath")
        val nxJson = PsiManager.getInstance(project).findFile(file) ?: throw NoSuchElementException("Could not find file for $filePath")
        val json = JsonParser.parseString(nxJson.text)
        return json.asJsonObject
    }
}