package com.example.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TcpRelayManager {
    private val tag = "TcpRelayManager"
    private var serverSocket: ServerSocket? = null
    private var listenJob: Job? = null
    private val activeConnections = AtomicInteger(0)
    private val totalBytesRx = AtomicLong(0)
    private val totalBytesTx = AtomicLong(0)

    val activeClients: Int
        get() = activeConnections.get()

    val bytesRx: Long
        get() = totalBytesRx.get()

    val bytesTx: Long
        get() = totalBytesTx.get()

    val isRunning: Boolean
        get() = serverSocket?.isClosed == false

    suspend fun startRelay(
        scope: CoroutineScope,
        localPort: Int,
        targetHost: String,
        targetPort: Int,
        bindToLan: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        stopRelay()
        try {
            val bindAddr = if (bindToLan) InetAddress.getByName("0.0.0.0") else InetAddress.getByName("127.0.0.1")
            val sSocket = ServerSocket(localPort, 50, bindAddr)
            serverSocket = sSocket

            listenJob = scope.launch(Dispatchers.IO) {
                while (isActive && !sSocket.isClosed) {
                    try {
                        val clientSocket = sSocket.accept()
                        handleClient(scope, clientSocket, targetHost, targetPort)
                    } catch (e: Exception) {
                        if (sSocket.isClosed) break
                        Log.w(tag, "Accept error", e)
                    }
                }
            }

            Result.success(sSocket.localPort)
        } catch (e: Exception) {
            Log.e(tag, "Failed to bind relay on port $localPort", e)
            stopRelay()
            Result.failure(e)
        }
    }

    private fun handleClient(
        scope: CoroutineScope,
        clientSocket: Socket,
        targetHost: String,
        targetPort: Int
    ) {
        scope.launch(Dispatchers.IO) {
            activeConnections.incrementAndGet()
            var remoteSocket: Socket? = null
            try {
                remoteSocket = Socket(targetHost, targetPort)
                val clientIn = clientSocket.getInputStream()
                val clientOut = clientSocket.getOutputStream()
                val remoteIn = remoteSocket.getInputStream()
                val remoteOut = remoteSocket.getOutputStream()

                val job1 = launch(Dispatchers.IO) {
                    pipe(clientIn, remoteOut, totalBytesRx)
                }
                val job2 = launch(Dispatchers.IO) {
                    pipe(remoteIn, clientOut, totalBytesTx)
                }

                job1.join()
                job2.join()
            } catch (e: Exception) {
                Log.d(tag, "Connection error: ${e.message}")
            } finally {
                try { clientSocket.close() } catch (_: Exception) {}
                try { remoteSocket?.close() } catch (_: Exception) {}
                activeConnections.decrementAndGet()
            }
        }
    }

    private fun pipe(input: InputStream, output: OutputStream, counter: AtomicLong) {
        val buffer = ByteArray(16384)
        try {
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                output.flush()
                counter.addAndGet(read.toLong())
            }
        } catch (_: Exception) {
        }
    }

    fun stopRelay() {
        try {
            listenJob?.cancel()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(tag, "Error closing relay", e)
        } finally {
            serverSocket = null
            listenJob = null
            activeConnections.set(0)
        }
    }
}
