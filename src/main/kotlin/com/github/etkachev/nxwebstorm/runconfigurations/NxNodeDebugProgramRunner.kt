package com.github.etkachev.nxwebstorm.runconfigurations

import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.configurations.WrappingRunConfiguration
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.debugger.LOG
import com.intellij.javascript.debugger.locationResolving.JSLocationResolver
import com.intellij.javascript.debugger.nodejs.NodeDebugConnector.getConnectorFileName
import com.intellij.javascript.nodejs.debug.NodeCommandLineOwner
import com.intellij.javascript.nodejs.debug.NodeDebugCommandLineConfigurator
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration
import com.intellij.javascript.nodejs.debug.NodeLocalDebuggableRunProfileState
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.javascript.nodejs.interpreter.remote.NodeJsRemoteInterpreter
import com.intellij.javascript.nodejs.library.core.NodeCoreLibraryConfigurator
import com.intellij.javascript.nodejs.library.core.NodeCoreLibraryManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.Strings
import com.intellij.ui.AppUIUtil
import com.intellij.util.PathUtil
import com.intellij.util.Url
import com.intellij.util.io.IOUtil
import com.intellij.util.text.SemVer
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.jetbrains.debugger.wip.WipLocalVmConnection
import com.jetbrains.nodeJs.NodeChromeDebugProcess
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration
import com.jetbrains.nodeJs.NodeJSFileFinder
import com.jetbrains.nodeJs.NodeOptionsVariableDebugConfigurator
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.io.File
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.function.Consumer
import kotlin.jvm.internal.Intrinsics

class NxNodeDebugProgramRunner : AsyncProgramRunner<RunnerSettings>() {
  private val eventLoopGroup = NioEventLoopGroup()

  override fun canRun(executorId: String, profile: RunProfile): Boolean {
    return Intrinsics.areEqual(
      "Debug",
      executorId
    ) && (profile is NodeDebugRunConfiguration || profile is NodeJSDebuggableConfiguration)
  }

  override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
    FileDocumentManager.getInstance().saveAllDocuments()
    val runProfile = environment.runProfile
    this.configureNodeCoreLibraries(runProfile, environment)

    if (runProfile is NodeDebugRunConfiguration && state is NodeLocalDebuggableRunProfileState && Registry.`is`("js.debugger.use.node.options") && !(runProfile as NodeDebugRunConfiguration).hasConfiguredDebugAddress()) {
      val tempDir = FileUtil.getTempDirectory()
      if (Strings.contains(
          tempDir as CharSequence,
          ' '.toString()
        ) || SystemInfo.isWindows && !IOUtil.isAscii(
          tempDir
        )
      ) {
        return runWithInspectArguments(state, environment)
      }
      val interpreter = this.getNodeJsInterpreter(runProfile, environment)
      val promise = this.getVersionPromise(interpreter)
      return promise.thenAsync { version ->
        val result = AsyncPromise<RunContentDescriptor?>()
        val app = ApplicationManager.getApplication()
        if (app.isDispatchThread) {
          val runPromise =
            if (NodeInterpreterUtil.supportsNodeOptions(interpreter, version)) runWithDebuggerConnector(
              state,
              environment,
              version
            ) else runWithInspectArguments(state, environment)
          runPromise.processed(result)
        } else {
          // ApplicationManager.getApplication().invokeLater()
        }
        result
      }
    }

    return this.runWithInspectArguments(state, environment)
  }

  override fun getRunnerId(): String {
    return "nx.node-js.debug.program.runner"
  }

  private fun runWithDebuggerConnector(
    state: NodeLocalDebuggableRunProfileState,
    environment: ExecutionEnvironment,
    nodeVersion: SemVer
  ): Promise<RunContentDescriptor?> {
    val debugConnectorPath: String = this.getDebugConnectorPath()
    val portsHandler = NxNodePortHandler()
    val serverBootstrap = ServerBootstrap()
    ((serverBootstrap.group(this.eventLoopGroup as EventLoopGroup)
      .channel(NioServerSocketChannel::class.java) as ServerBootstrap).childHandler(
      object : ChannelInitializer<SocketChannel>() {
        @Throws(Exception::class)
        override fun initChannel(ch: SocketChannel) {
          ch.pipeline().addLast(portsHandler as ChannelHandler)
        }
      })
      .option(ChannelOption.SO_BACKLOG, 128) as ServerBootstrap).childOption(ChannelOption.SO_KEEPALIVE, true)
    val channel = serverBootstrap.bind(0).sync().channel()
    val localAddress = channel.localAddress()
    if (localAddress == null) {
      throw TypeCastException("null cannot be cast to non-null type java.net.InetSocketAddress")
    } else {
      val publishPort = (localAddress as InetSocketAddress).port
      LOG.info("JSDebugger publish port: $publishPort")
      val debugProcessPromise: AsyncPromise<NodeChromeDebugProcess> = AsyncPromise()
      val runProfile: RunProfile = environment.runProfile
      var profileInterpreter = (runProfile as NodeDebugRunConfiguration).interpreter
      if (profileInterpreter !is NodeJsLocalInterpreter) {
        profileInterpreter = null
      }
      var interpreter = profileInterpreter as NodeJsLocalInterpreter?
      if (!this.accept(interpreter)) {
        interpreter = this.findLocalNode()
      }
      val interpreterPath = interpreter?.interpreterSystemIndependentPath
      val nodeDebugConfigurator = NodeOptionsVariableDebugConfigurator(
        publishPort,
        debugConnectorPath,
        nodeVersion,
        debugProcessPromise,
        interpreterPath
      )
      val stateExecute: Promise<ExecutionResult> =
        state.execute(nodeDebugConfigurator as CommandLineDebugConfigurator)
      stateExecute.onError(
        (Consumer<Throwable>
        { channel.close() as Throwable }) as Consumer<Throwable>
      )
      return stateExecute.then { executionResult ->
        val newSession = startSession(
          environment,
          state as RunProfileState,
          (object : XDebugProcessStarter() {
            override fun start(session: XDebugSession): XDebugProcess {
              session.addSessionListener(((object : XDebugSessionListener {
                override fun sessionStopped() {
                  channel.close()
                }
              }) as XDebugSessionListener))
              val connection = WipLocalVmConnection(null as Url?)
              val process = NodeChromeDebugProcess(
                session,
                NodeJSFileFinder(environment.project),
                connection,
                executionResult
              )

              portsHandler.setDebugProcess(process)
              debugProcessPromise.setResult(process)
              return process
            }
          }) as XDebugProcessStarter
        )
        newSession
      }
    }
  }

  private fun findLocalNode(): NodeJsLocalInterpreter? {
    val fromPath = NodeJsLocalInterpreterManager.getInstance().detectMostRelevant()
    return if (accept(fromPath)) {
      fromPath
    } else {
      val interpreterManager = NodeJsLocalInterpreterManager.getInstance()
      val iterator = interpreterManager.interpreters.iterator()
      var interpreter: NodeJsLocalInterpreter?
      do {
        if (!iterator.hasNext()) {
          return null
        }
        interpreter = iterator.next()
      } while (!accept(interpreter))
      interpreter
    }
  }

  private fun accept(interpreter: NodeJsLocalInterpreter?): Boolean {
    return interpreter != null && !interpreter.isElectron
  }

  private fun getDebugConnectorPath(): String {
    val app = ApplicationManager.getApplication()
    if (app.isInternal && PluginManagerCore.isRunningFromSources()) {
      val rawFile = NxNodeDebugProgramRunner::class.java.classLoader.getResource("debugConnector.js")
      if (rawFile != null) {
        try {
          val compiledFilePath = File(rawFile.toURI()).absolutePath
          val sourceFilePath = compiledFilePath.replace(
            "/out/classes/production/intellij.javascript.chrome.connector/",
            "/plugins/JavaScriptDebugger/ChromeConnector/nodeDebugInitializer/",
            false
          )
          if (File(sourceFilePath).exists()) {
            return sourceFilePath
          }
        } catch (var4: Throwable) {
        }
      }
    }
    val fileName = getConnectorFileName()
    val tempFile = File(FileUtil.getTempDirectory(), fileName)
    if (!tempFile.exists()) {
      val bundledFile: File = this.getBundledDebugConnectorFile()
      FileUtil.copy(bundledFile, tempFile)
    }
    return tempFile.absolutePath
  }

  private fun getBundledDebugConnectorFile(): File {
    val jarPathForClass = PathUtil.getJarPathForClass(NxNodeDebugProgramRunner::class.java)
    val connectorFile: File
    connectorFile = if (jarPathForClass.endsWith(".jar", false)) {
      val jarFile = File(jarPathForClass)
      if (!jarFile.isFile) {
        throw RuntimeException("jar file cannot be null")
      }
      File(jarFile.parentFile, "debugConnector.js")
    } else {
      val connector = NxNodeDebugProgramRunner::class.java.classLoader.getResource("debugConnector.js")
        ?: throw (RuntimeException("Resource not found: debugConnector.js") as Throwable)
      File(connector.toURI())
    }
    return connectorFile
  }

  private fun configureNodeCoreLibraries(runProfile: RunProfile, environment: ExecutionEnvironment) {
    var thisRunProfile: RunProfile? = this.getRunProfile(runProfile)
    if (thisRunProfile !is NodeDebugRunConfiguration) {
      thisRunProfile = null
    }

    val nodeDebugRunConfig = thisRunProfile as NodeDebugRunConfiguration?
    if (nodeDebugRunConfig != null) {
      val nodeDebugInterpreter = nodeDebugRunConfig.interpreter
      if (nodeDebugInterpreter != null) {
        val interpreter: NodeJsInterpreter = nodeDebugInterpreter
        val nodeCoreLibConfigurator = NodeCoreLibraryConfigurator.getInstance(environment.project)
        val configuredVersion = nodeCoreLibConfigurator.configuredCoreLibraryVersion
        if (configuredVersion != null && nodeCoreLibConfigurator.isAvailable(interpreter)) {
          interpreter.provideCachedVersionOrFetch().then { param ->
            if (param != null && Intrinsics.areEqual(param, configuredVersion.nodeVersion) xor true) {
              AppUIUtil.invokeLaterIfProjectAlive(environment.project) {
                val nodeCoreLibMan = NodeCoreLibraryManager.getInstance(environment.project)
                val coreLibManagerRoots = nodeCoreLibMan.associatedRoots
                nodeCoreLibConfigurator.configureAndAssociateWith(
                  interpreter,
                  param,
                  coreLibManagerRoots.toMutableList()
                ) { JSLocationResolver.instance.dropCache(runProfile) }
              }
            }
          }
        }
        return
      }
    }
  }

  private fun getRunProfile(runProfile: RunProfile): RunProfile {
    return if (runProfile is WrappingRunConfiguration<*>) {
      val runProfilePeer = (runProfile as WrappingRunConfiguration<*>).peer
      runProfilePeer as RunProfile
    } else {
      runProfile
    }
  }

  private fun getDebugAddress(configuration: RunProfile, state: RunProfileState): InetSocketAddress {
    val serverSocket = ServerSocket(0)
    val availablePort = serverSocket.localPort

    val tempSocketAddress = InetSocketAddress("localhost", availablePort)
    var runProfile: RunProfile? = configuration
    if (configuration !is NodeJSDebuggableConfiguration) {
      runProfile = null
    }
    var finalDebugAddress: InetSocketAddress = tempSocketAddress
    val nodeJsDebugConfig = runProfile as NodeJSDebuggableConfiguration?
    if (nodeJsDebugConfig != null) {
      finalDebugAddress = nodeJsDebugConfig.computeDebugAddress(state)
    }
    return finalDebugAddress
  }

  private fun getVersionPromise(interpreter: NodeJsInterpreter): Promise<SemVer> {
    return (interpreter as? NodeJsLocalInterpreter)?.provideCachedVersionOrFetch()
      ?: if (NodeJsRemoteInterpreter.isDockerCompose(interpreter)) resolvedPromise(
        SemVer(
          "8.0.0",
          8,
          0,
          0
        )
      ) else resolvedPromise(SemVer("0.0.0", 0, 0, 0))
  }

  private fun getNodeJsInterpreter(runProfile: RunProfile, environment: ExecutionEnvironment): NodeJsInterpreter {
    return (runProfile as NodeDebugRunConfiguration).interpreter ?: NodeJsInterpreterRef.createProjectRef()
      .resolveNotNull(environment.project)
  }

  private fun runWithInspectArguments(
    state: RunProfileState,
    environment: ExecutionEnvironment
  ): Promise<RunContentDescriptor?> {
    val runProfile = environment.runProfile
    val thisRunProfile = this.getRunProfile(runProfile)
    val debugAddress = this.getDebugAddress(runProfile, state)

    val interpreter: NodeJsInterpreter =
      (thisRunProfile as NodeDebugRunConfiguration).interpreter ?: NodeJsInterpreterRef.createProjectRef()
        .resolveNotNull(environment.project)
    val configurator = NodeDebugCommandLineConfigurator.Companion.new(debugAddress.port, interpreter)

    val nodeDebugRunProfileState: NodeLocalDebuggableRunProfileState = state as NodeLocalDebuggableRunProfileState
    val executionResultPromise = nodeDebugRunProfileState.execute(configurator)

    return executionResultPromise.then { executionResult: ExecutionResult? ->
      startSession(
        environment,
        state,
        NewDebugProcessStarter(runProfile, debugAddress, executionResult, environment, interpreter)
      )
    }
  }

  private fun startSession(
    environment: ExecutionEnvironment,
    state: RunProfileState,
    starter: XDebugProcessStarter
  ): RunContentDescriptor? {
    val newSession = XDebuggerManagerImpl.getInstance(environment.project).startSession(environment, starter)
    val runContentDescriptor = newSession.runContentDescriptor
    val view = newSession.consoleView
    if (state is NodeCommandLineOwner && view != null) {
      val nodeCommandLine = state as NodeCommandLineOwner
      val debugProcess = newSession.debugProcess
      nodeCommandLine.foldCommandLine(view, debugProcess.processHandler)
    }
    var chromeDebugProcess: XDebugProcess? = newSession.debugProcess
    if (chromeDebugProcess !is NodeChromeDebugProcess) {
      chromeDebugProcess = null
    }
    val var10 = chromeDebugProcess as NodeChromeDebugProcess?
    var10?.addLanguageConsoleTab()
    return runContentDescriptor
  }
}
