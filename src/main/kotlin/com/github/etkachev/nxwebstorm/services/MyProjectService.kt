package com.github.etkachev.nxwebstorm.services

import com.intellij.openapi.project.Project
import com.github.etkachev.nxwebstorm.MyBundle

class MyProjectService(project: Project) {

  init {
    println(MyBundle.message("projectService", project.name))
  }
}
