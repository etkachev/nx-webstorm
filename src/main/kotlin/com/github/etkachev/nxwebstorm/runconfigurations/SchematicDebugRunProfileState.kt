package com.github.etkachev.nxwebstorm.runconfigurations

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner

class SchematicDebugRunProfileState(private val environment: ExecutionEnvironment) : RunProfileState {
  override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
    val hi = environment
    return null
  }
}
