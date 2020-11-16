package com.github.etkachev.nxwebstorm.runconfigurations

import com.github.etkachev.nxwebstorm.models.RunSchematicConfig
import com.github.etkachev.nxwebstorm.utils.getCommandArguments
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.intellij.javascript.nodejs.NodeStackTraceFilter
import com.intellij.javascript.nodejs.debug.NodeLocalDebuggableRunProfileStateSync
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.StandardCharsets

fun nodeOptions(): List<String> {
  return listOf(NodeCommandLineUtil.INSPECT)
}

class SchematicDebugProfileState(
  private val environment: ExecutionEnvironment,
  private val config: RunSchematicConfig
) : NodeLocalDebuggableRunProfileStateSync() {
  private val interpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
  override fun executeSync(configurator: CommandLineDebugConfigurator?): ExecutionResult {
    return executeDebugConfig(this.interpreterRef, this.environment, configurator, this.config)
    // val interpreter: NodeJsInterpreter = this.interpreterRef.resolveNotNull(this.environment.project)
    // val root = this.environment.project.basePath ?: ""
    // val commandLine = NodeCommandLineUtil.createCommandLine()
    // val port = (configurator as DebugPortConfigurator).debugPort
    // NodeCommandLineUtil.configureCommandLine(
    //   commandLine,
    //   configurator
    // ) { configureCommandLine(root, commandLine, interpreter, config) }
    // val processHandler: ProcessHandler = NodeCommandLineUtil.createProcessHandler(commandLine, true)
    // ProcessTerminatedListener.attach(processHandler)
    // val console: ConsoleView = createConsole(this.environment, processHandler, commandLine.workDirectory)
    // console.attachToProcess(processHandler)
    // // NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, nodeOptions(), port, true, interpreter, true)
    // // val debugger = XDebuggerManager.getInstance(this.environment.project)
    // // debugger.startSession(this.environment, NxDebugProcessStarter(config, environment, console))
    // return DefaultExecutionResult(console, processHandler)
  }

  // private fun configureCommandLine(root: String, commandLine: GeneralCommandLine, interpreter: NodeJsInterpreter) {
  //
  //   commandLine.withCharset(StandardCharsets.UTF_8)
  //   // CommandLineUtil.setWorkingDirectory(commandLine, File(this.runSettings.nxFilePath).parentFile, false)
  //   // if (this.nxPackage is YarnPnpNodePackage) {
  //   //   this.nxPackage.addYarnRunToCommandLine(commandLine, this.environment.project, interpreter, null as String?)
  //   // } else {
  //   //   commandLine.addParameter(getNxBinFile(this.nxPackage).absolutePath)
  //   // }
  //   // commandLine.addParameter("$rootPath/$cli")
  //   val (cli, exec) = config.cli.data
  //   val path = "$cli/$exec"
  //   commandLine.setWorkDirectory("$root/$cli")
  //
  //   commandLine.addParameters("$root/$path", config.command, config.name)
  //   commandLine.addParameters(getCommandArguments(config.args))
  //   commandLine.addParameters(config.additionalArgs)
  //   // commandLine.addParameters(this.runSettings.tasks)
  //   // commandLine.addParameters(this.runSettings.arguments?.let { ParametersListUtil.parse(it) } ?: emptyList())
  //
  //   NodeCommandLineConfigurator.find(interpreter).configure(commandLine)
  // }

  // private fun createConsole(processHandler: ProcessHandler, cwd: File?): ConsoleView {
  //   val project: Project = this.environment.project
  //   val consoleView = NodeCommandLineUtil.createConsole(processHandler, project, false)
  //   consoleView.addMessageFilter(NodeStackTraceFilter(project, cwd))
  //   consoleView.addMessageFilter(NodeConsoleAdditionalFilter(project, cwd))
  //   consoleView.addMessageFilter(TypeScriptErrorConsoleFilter(project, cwd))
  //   return consoleView
  // }
}

fun executeDebugConfig(
  interpreterRef: NodeJsInterpreterRef,
  environment: ExecutionEnvironment,
  configurator: CommandLineDebugConfigurator?,
  config: RunSchematicConfig
): ExecutionResult {
  val interpreter: NodeJsInterpreter = interpreterRef.resolveNotNull(environment.project)
  val root = environment.project.basePath ?: ""
  val commandLine = NodeCommandLineUtil.createCommandLine()
  NodeCommandLineUtil.configureCommandLine(
    commandLine,
    configurator
  ) { configureCommandLine(root, commandLine, interpreter, config) }
  val processHandler: ProcessHandler = NodeCommandLineUtil.createProcessHandler(commandLine, true)
  ProcessTerminatedListener.attach(processHandler)
  val console: ConsoleView = createConsole(environment, processHandler, commandLine.workDirectory)
  console.attachToProcess(processHandler)
  return DefaultExecutionResult(console, processHandler)
}

fun configureCommandLine(
  root: String,
  commandLine: GeneralCommandLine,
  interpreter: NodeJsInterpreter,
  config: RunSchematicConfig
) {

  commandLine.withCharset(StandardCharsets.UTF_8)
  val (cli, exec) = config.cli.data
  val path = "$cli/$exec"
  commandLine.setWorkDirectory("$root/$cli")

  commandLine.addParameters("$root/$path", config.command, config.name)
  commandLine.addParameters(getCommandArguments(config.args))
  commandLine.addParameters(config.additionalArgs)

  NodeCommandLineConfigurator.find(interpreter).configure(commandLine)
}

fun createConsole(environment: ExecutionEnvironment, processHandler: ProcessHandler, cwd: File?): ConsoleView {
  val project: Project = environment.project
  val consoleView = NodeCommandLineUtil.createConsole(processHandler, project, false)
  consoleView.addMessageFilter(NodeStackTraceFilter(project, cwd))
  consoleView.addMessageFilter(NodeConsoleAdditionalFilter(project, cwd))
  consoleView.addMessageFilter(TypeScriptErrorConsoleFilter(project, cwd))
  return consoleView
}
