package com.github.etkachev.nxwebstorm.runconfigurations

import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.util.Function
import com.jetbrains.debugger.wip.WipRemoteVmConnection
import com.jetbrains.nodeJs.NodeChromeDebugProcess
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtf8Writer
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.jetbrains.concurrency.Promise
import org.jetbrains.debugger.connection.VmConnection
import org.jetbrains.wip.WipVm
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class NxNodePortHandler : ChannelInboundHandlerAdapter() {
  private val firstPortFuture: CompletableFuture<Pair<Int, Channel>> = CompletableFuture()
  private val lock = Any()
  private val openedConnections: ArrayList<Pair<Int, Channel>> = ArrayList()
  private var debugProcess: NodeChromeDebugProcess? = null
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val byteBuf = msg as ByteBuf
    try {
      val byteBufString = byteBuf.toString(Charset.forName("UTF-8"))
      val debugPort = byteBufString.toInt()
      if (!firstPortFuture.isDone) {
        firstPortFuture.complete(Pair<Int, Channel>(debugPort, ctx.channel()))
        return
      }
      val var14 = lock
      synchronized(var14) {
        val process: NodeChromeDebugProcess? = debugProcess
        if (process != null) {
        } else {
          openedConnections.add(Pair<Int, Channel>(debugPort, ctx.channel()))
        }
      }
    } finally {
      byteBuf.release()
    }
  }

  fun continueVm(channel: Channel, vm: WipVm) {
    vm.addReadyListener {
      val buffer = ByteBufAllocator.DEFAULT.buffer()
      ByteBufUtf8Writer(buffer).write("continue")
      channel.writeAndFlush(buffer)
    }
  }

  fun setDebugProcess(debugProcess: NodeChromeDebugProcess) {
    val debugProcessConnection: VmConnection<*> = debugProcess.connection
    val vmConnection = debugProcessConnection as WipRemoteVmConnection
    firstPortFuture.thenAccept((Consumer<Pair<Number, Channel>> { portChannel ->
      val port = portChannel.component1().toInt()
      val channel = portChannel.component2()
      vmConnection.open(InetSocketAddress(NodeCommandLineUtil.getNodeLoopbackAddress(), port))
        .then { wipVm: WipVm ->
          val portsHandler = this@NxNodePortHandler
          portsHandler.continueVm(channel, wipVm)
        }
    }))
    val var3 = lock
    synchronized(var3) {
      this.debugProcess = debugProcess
      val iterator = openedConnections.iterator()
      while (iterator.hasNext()) {
        val portAndChannel: Pair<Int, Channel> = iterator.next()
        addChildVm(debugProcess, (portAndChannel.first as Number).toInt()).then(
          NxNodePortHandlerInline(portAndChannel, this)
        )
      }
      openedConnections.clear()
    }
  }

  private fun addChildVm(process: NodeChromeDebugProcess, port: Int): Promise<WipVm> {
    return process.connectChildProcess(InetSocketAddress(NodeCommandLineUtil.getNodeLoopbackAddress(), port))
  }
}

internal class NxNodePortHandlerInline(
  private val portChannel: Pair<Int, Channel>,
  private val tThis: NxNodePortHandler
) :
  Function<WipVm?, Any?> {

  override fun `fun`(it: WipVm?) {
    val portHandler = tThis
    val channel = portChannel.second
    portHandler.continueVm(channel, it!!)
  }
}
