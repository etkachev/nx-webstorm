package com.github.etkachev.nxwebstorm.runconfigurations

// import com.intellij.execution.process.ProcessAdapter
// import com.intellij.execution.process.ProcessEvent
// import com.jetbrains.debugger.wip.WipLocalVmConnection
import com.github.etkachev.nxwebstorm.models.RunSchematicConfig
import com.github.etkachev.nxwebstorm.utils.getCommandArguments
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.javascript.debugger.DebuggableFileFinder
import com.intellij.javascript.debugger.JavaScriptDebugProcess
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.intellij.javascript.nodejs.NodeStackTraceFilter
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.NullableConsumer
import com.intellij.util.PathMappingSettings
import com.intellij.util.text.SemVer
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.jetbrains.debugger.wip.WipLocalVmConnection
import com.jetbrains.nodeJs.NodeChromeDebugProcess
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration
import com.jetbrains.nodeJs.NodeJSFileFinder
import com.jetbrains.nodeJs.createNodeJsDebugProcess
import com.jetbrains.nodeJs.createRemoteConnection
import icons.PluginIcons
import org.jetbrains.annotations.NotNull
import org.jetbrains.io.LocalFileFinder
import java.io.File
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import javax.swing.Icon
import kotlin.jvm.functions.Function1
import kotlin.jvm.internal.Intrinsics

// import com.jetbrains.nodeJs.createRemoteConnection

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
    // label22@{
    val var3 = thisRunProfile as NodeJSDebuggableConfiguration?
    if (var3 != null) {
      process = var3.createDebugProcess(debugAddress, session, executionResult, environment)
      // if (var4 != null) {
      //   // break@label22
      // }
    } else {
      val thisProject = environment.project
      val newConnection = WipLocalVmConnection()
      process = NodeChromeDebugProcess(session, NodeJSFileFinder(thisProject), newConnection, executionResult)
      // process = createNodeJsDebugProcess(
      //   debugAddress,
      //   session,
      //   executionResult,
      //   NodeJSFileFinder(thisProject)
      // )
    }
    // val var5: InetSocketAddress = debugAddress
    // val var10001: NodeJsInterpreter = interpreter
    //@TODO may add this back below
    // val thisProject = environment.project
    // var4 = createNodeJsDebugProcess(
    //   debugAddress,
    //   session,
    //   executionResult,
    //   NodeJSFileFinder(thisProject)
    // )
    // }
    // val process = var4
    if (process is JavaScriptDebugProcess<*>) {
      val var6: NodeJsInterpreter = interpreter
      var6.provideCachedVersionOrFetch {
        // if (version != null && version.major <= 7) {
        //   process.connection.executeOnStart(null.INSTANCE as Function1<*, *>?)
        // }
      }
    }
    return process
  }
}

class NxDebugProcessStarter(
  private val config: RunSchematicConfig,
  private val environment: ExecutionEnvironment,
  private val portNumber: Int
) :
  XDebugProcessStarter() {
  private val interpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
  override fun start(session: XDebugSession): XDebugProcess {
    val fileFinder = NodeJSFileFinder(environment.project)
    // val currentMappings: List<PathMappingSettings.PathMapping> = listOf()
    // val mappings = this.createBiMapMappings(currentMappings)
    // val fileFinder = NxDebuggableFileFinder()
    val socketAddress = InetSocketAddress("localhost", portNumber)
    val executionResult = executeDebugConfig(this.interpreterRef, this.environment, null, config)
    val process = createNodeJsDebugProcess(socketAddress, session, executionResult, fileFinder)
    return process
    // val currentMappings: List<PathMappingSettings.PathMapping> = listOf()
    // val mappings = this.createBiMapMappings(currentMappings)
    // val fileFinder = RemoteDebuggingFileFinder(mappings)
    // val connection = WipLocalVmConnection()
    // val executionResult = executeDebugConfig(this.interpreterRef, this.environment, null, config)
    // val process = NodeChromeDebugProcess(session, fileFinder, connection, executionResult)
    //
    // val processHandler = executionResult.processHandler
    // val debugPort = 8000
    // val socketAddress = InetSocketAddress("localhost", debugPort)
    //
    // if (processHandler == null || processHandler.isStartNotified) {
    //   connection.open(socketAddress)
    // } else {
    //   processHandler.addProcessListener(object : ProcessAdapter() {
    //     override fun startNotified(event: ProcessEvent) {
    //       connection.open(socketAddress)
    //     }
    //   })
    // }
    // return process
    // return NxDebugProcess(session, config, environment)
  }

  /**
   * Convert [PathMapping] to NodeJs debugger path mapping format.
   *
   * Docker uses the same project structure for dependencies in the folder node_modules. We map the source code and
   * the dependencies in node_modules folder separately as the node_modules might not exist in the local project.
   */
  private fun createBiMapMappings(pathMapping: List<PathMappingSettings.PathMapping>): BiMap<String, VirtualFile> {
    val mappings = HashBiMap.create<String, VirtualFile>(pathMapping.size)

    listOf(".", NODE_MODULES).forEach { subPath ->
      pathMapping.forEach {
        val remotePath = FileUtil.toCanonicalPath("$TASK_PATH/${it.remoteRoot}/$subPath")
        LocalFileFinder.findFile("${it.localRoot}/$subPath")?.let { localFile ->
          mappings.putIfAbsent("file://$remotePath", localFile)
        }
      }
    }

    return mappings
  }

  private companion object {
    const val TASK_PATH = "/var/task"
    const val NODE_MODULES = "node_modules"
  }
}

// class NxDebuggableFileFinder : DebuggableFileFinder {
//   override fun getRemoteUrls(file: VirtualFile): List<Url> {
//     return listOf(Urls.newFromVirtualFile(file))
//   }
// }

class NxProgramRunner(
  private val config: RunSchematicConfig,
  private val portNumber: Int,
  private val executor: Executor
) : ProgramRunner<RunnerSettings> {
  override fun getRunnerId(): String {
    return "nx-webstorm.debug.schematics"
  }

  override fun canRun(executorId: String, profile: RunProfile): Boolean {
    val valid = executor is DefaultDebugExecutor
    return valid
  }

  override fun execute(environment: ExecutionEnvironment) {
    val runProfileState = SchematicDebugProfileState(environment, config)
    // ExecutionManager.getInstance(environment.project)
    val session = XDebuggerManager.getInstance(environment.project)
      .startSessionAndShowTab("Nx Debug", null, false, NxDebugProcessStarter(config, environment, portNumber))
    val hi = 1
  }
}

class NxDebugProcess(
  session: @NotNull XDebugSession,
  private val config: RunSchematicConfig,
  private val environment: ExecutionEnvironment
) : XDebugProcess(session) {
  private val interpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()

  override fun getEditorsProvider(): XDebuggerEditorsProvider {
    return NxDebugEditor()
  }

  override fun sessionInitialized() {
  }

  // override fun createConsole(): ExecutionConsole {
  //   // val interpreter: NodeJsInterpreter = this.interpreterRef.resolveNotNull(this.environment.project)
  //   // val root = this.environment.project.basePath ?: ""
  //   // val commandLine = NodeCommandLineUtil.createCommandLine()
  //   // NodeCommandLineUtil.configureCommandLine(
  //   //   commandLine,
  //   //   null
  //   // ) { this.configureCommandLine(root, commandLine, interpreter) }
  //   // val processHandler: ProcessHandler = NodeCommandLineUtil.createProcessHandler(commandLine, true)
  //   // ProcessTerminatedListener.attach(processHandler)
  //   // val console: ConsoleView = this.createConsole(processHandler, commandLine.workDirectory)
  //   // console.attachToProcess(processHandler)
  //   // return console
  // }

  private fun configureCommandLine(root: String, commandLine: GeneralCommandLine, interpreter: NodeJsInterpreter) {

    commandLine.withCharset(StandardCharsets.UTF_8)
    // CommandLineUtil.setWorkingDirectory(commandLine, File(this.runSettings.nxFilePath).parentFile, false)
    // if (this.nxPackage is YarnPnpNodePackage) {
    //   this.nxPackage.addYarnRunToCommandLine(commandLine, this.environment.project, interpreter, null as String?)
    // } else {
    //   commandLine.addParameter(getNxBinFile(this.nxPackage).absolutePath)
    // }
    // commandLine.addParameter("$rootPath/$cli")
    val (cli, exec) = config.cli.data
    val path = "$cli/$exec"
    commandLine.setWorkDirectory("$root/$cli")

    commandLine.addParameters("$root/$path", config.command, config.name)
    commandLine.addParameters(getCommandArguments(config.args))
    // commandLine.addParameters(this.runSettings.tasks)
    // commandLine.addParameters(this.runSettings.arguments?.let { ParametersListUtil.parse(it) } ?: emptyList())

    NodeCommandLineConfigurator.find(interpreter).configure(commandLine)
  }

  private fun createConsole(processHandler: ProcessHandler, cwd: File?): ConsoleView {
    val project: Project = this.environment.project
    val consoleView = NodeCommandLineUtil.createConsole(processHandler, project, false)
    consoleView.addMessageFilter(NodeStackTraceFilter(project, cwd))
    consoleView.addMessageFilter(NodeConsoleAdditionalFilter(project, cwd))
    consoleView.addMessageFilter(TypeScriptErrorConsoleFilter(project, cwd))
    return consoleView
  }
}

class NxDebugEditor : XDebuggerEditorsProvider() {
  override fun getFileType(): FileType {
    return NxFileType()
  }
}

class NxFileType : FileType {
  override fun getName(): String = "NxPlugin"

  override fun getDescription(): String = ""

  override fun getDefaultExtension(): String = ".nx"

  override fun getIcon(): Icon? = PluginIcons.NRWL_ICON

  override fun isBinary(): Boolean = true

  override fun isReadOnly(): Boolean = true

  override fun getCharset(file: VirtualFile, content: ByteArray): String? = null
}
