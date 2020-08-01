package com.github.etkachev.nxwebstorm.actions

import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager
import com.google.gson.JsonParser

class Welcome: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }
        val proj = e.project!!
        val file = ProjectRootManager.getInstance(proj).contentRoots[0].findFileByRelativePath("nx.json")
        if (file == null) {
            Messages.showMessageDialog(e.project, "This is no Nx", "Sorry", Messages.getWarningIcon())
            return
        }
        val nxJson = PsiManager.getInstance(e.project!!).findFile(file) ?: return
        val json = JsonParser.parseString(nxJson.text)
        val projects = json.asJsonObject.getAsJsonObject("projects").keySet()
        val firstFew = projects.take(5).joinToString(",")
        Messages.showMessageDialog(e.project, "First projects are $firstFew", "Hello from Nx", Messages.getInformationIcon())
    }
}