package com.github.etkachev.nxwebstorm.runconfigurations

import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.debugger.JavaScriptDebugProcess
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.jetbrains.debugger.wip.WipLocalVmConnection
import com.jetbrains.nodeJs.NodeChromeDebugProcess
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration
import com.jetbrains.nodeJs.NodeJSFileFinder
import java.net.InetSocketAddress

class NewDebugProcessStarter(
  private val runProfile: RunProfile,
  private val debugAddress: InetSocketAddress,
  private val executionResult: ExecutionResult?,
  private val environment: ExecutionEnvironment,
  private val interpreter: NodeJsInterpreter
) : XDebugProcessStarter() {
  override fun start(session: XDebugSession): XDebugProcess {
    var thisRunProfile: RunProfile? = runProfile
    if (thisRunProfile !is NodeJSDebuggableConfiguration) {
      thisRunProfile = null
    }
    val process: XDebugProcess
    val var3 = thisRunProfile as NodeJSDebuggableConfiguration?
    if (var3 != null) {
      process = var3.createDebugProcess(debugAddress, session, executionResult, environment)
    } else {
      val thisProject = environment.project
      val newConnection = WipLocalVmConnection()
      process = NodeChromeDebugProcess(session, NodeJSFileFinder(thisProject), newConnection, executionResult)
    }
    if (process is JavaScriptDebugProcess<*>) {
      val var6: NodeJsInterpreter = interpreter
      var6.provideCachedVersionOrFetch {
      }
    }
    return process
  }
}
