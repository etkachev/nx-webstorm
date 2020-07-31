package com.github.etkachev.nxwebstorm.actions

import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class Welcome: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }
        val file = VirtualFileManager.getInstance().refreshAndFindFileByUrl("nx.json")
        if (file == null) {
            Messages.showMessageDialog(e.project, "This is no Nx", "Sorry", Messages.getWarningIcon())
            return
        }
        val nxJson = PsiManager.getInstance(e.project!!).findFile(file) ?: return
        val psi = nxJson.viewProvider.getPsi(JsonLanguage.INSTANCE)
        psi.accept(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
            }
        })
        Messages.showMessageDialog(e.project, "Hello from Nx", "Title Here", Messages.getInformationIcon())
    }
}