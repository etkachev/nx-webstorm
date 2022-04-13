package com.github.etkachev.nxwebstorm.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager

fun getRootPsiDirectory(project: Project): PsiDirectory? {
  val root = ProjectRootManager.getInstance(project).contentRootsFromAllModules[0]
  return PsiManager.getInstance(project).findDirectory(root)
}

/**
 * via recursion find the psi directory based on split directory array.
 * example directory: "/node_modules/nrwl/angular" would be ["node_modules", "nrwl", "angular"]
 */
fun findPsiDirectoryBySplitFolders(
  dir: Array<String>,
  rootPsiDirectory: PsiDirectory?,
  dirIndex: Int = 0,
  checkedDirectory: PsiDirectory? = null
): PsiDirectory? {
  if (dir.count() == 0 || rootPsiDirectory == null) {
    return null
  }

  // if reached end of array, return currentDirectory
  if (dirIndex == dir.count()) {
    return checkedDirectory
  }

  val currentPsiDir = (if (dirIndex == 0) rootPsiDirectory else checkedDirectory) ?: return null

  val currentDir = dir[dirIndex]
  val subPsiDir = currentPsiDir.findSubdirectory(currentDir) ?: return null
  return findPsiDirectoryBySplitFolders(dir, rootPsiDirectory, dirIndex + 1, subPsiDir)
}
