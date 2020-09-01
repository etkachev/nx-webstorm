package com.github.etkachev.nxwebstorm.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.github.etkachev.nxwebstorm.utils.GetNxData

class Welcome : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }
        val proj = e.project!!
        val projects = try {
            GetNxData().getProjects(proj)
        } catch (e: NoSuchElementException) {
            null
        }

        if (projects == null) {
            Messages.showMessageDialog(e.project, "This is no nx.json", "Sorry", Messages.getWarningIcon())
            return
        }
        val firstFew = projects.take(5).joinToString(",")
        Messages.showMessageDialog(
            e.project, "First projects are $firstFew", "Hello from Nx",
            Messages.getInformationIcon()
        )
    }
}
