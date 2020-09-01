package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiManager

class ComputeReadJsonFile(private val project: Project, private val filePath: String) :
    ThrowableComputable<JsonObject, NoSuchElementException> {
    override fun compute(): JsonObject {
        val file = ProjectRootManager.getInstance(project).contentRoots[0].findFileByRelativePath(filePath)
            ?: throw NoSuchElementException("Could not find file for $filePath")
        val fileContents = PsiManager.getInstance(project).findFile(file)
            ?: throw NoSuchElementException("Could not find file for $filePath")
        val json = JsonParser.parseString(fileContents.text)
        return json.asJsonObject
    }
}

class ReadJsonFile {
    fun fromFileUrl(project: Project, filePath: String): JsonObject {
        return ApplicationManager.getApplication().runReadAction(ComputeReadJsonFile(project, filePath))
    }
}
