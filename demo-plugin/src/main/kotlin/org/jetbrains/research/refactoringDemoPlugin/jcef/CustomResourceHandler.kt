package org.jetbrains.research.refactoringDemoPlugin.jcef

import java.net.URLConnection
import org.cef.callback.CefCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.io.InputStream

class CustomResourceHandler : CefResourceHandler {
    private var state: ResourceHandlerState = ClosedConnection

    override fun processRequest(
        cefRequest: CefRequest,
        cefCallback: CefCallback
    ): Boolean {
        val processedUrl = cefRequest.getURL() ?: return false
        val pathToResource = processedUrl.replace("http://${JcefWindowService.pluginResourcesDomain}", "jcef")
        val newUrl = CustomResourceHandler::class.java.classLoader.getResource(pathToResource) ?: return false
        state.close()
        state = OpenedConnection(newUrl.openConnection())
        cefCallback.Continue()
        return true
    }

    override fun getResponseHeaders(
        response: CefResponse?,
        responseLength: IntRef?,
        redirectUrl: StringRef?
    ) {
        state.getResponseHeaders(response, responseLength, redirectUrl)
    }

    override fun readResponse(
        dataOut: ByteArray?,
        bytesToRead: Int,
        bytesRead: IntRef?,
        callback: CefCallback?
    ): Boolean {
        return state.readResponse(dataOut, bytesToRead, bytesRead, callback)
    }

    override fun cancel() {
        state.close()
        state = ClosedConnection
    }
}

sealed class ResourceHandlerState {
    abstract fun getResponseHeaders(
        response: CefResponse?,
        responseLength: IntRef?,
        redirectUrl: StringRef?
    )

    abstract fun readResponse(
        dataOut: ByteArray?,
        bytesToRead: Int,
        bytesRead: IntRef?,
        callback: CefCallback?
    ): Boolean

    abstract fun close()
}

class OpenedConnection(val connection: URLConnection) : ResourceHandlerState() {
    private val inputStream: InputStream by lazy { connection.getInputStream() }

    override fun getResponseHeaders(
        response: CefResponse?,
        responseLength: IntRef?,
        redirectUrl: StringRef?
    ) {
        response ?: return
        try {
            val url = connection.url.toString()
            when {
                url.contains("css") -> response.mimeType = "text/css"
                url.contains("js") -> response.mimeType = "text/javascript"
                url.contains("html") -> response.mimeType = "text/html"
                else -> response.mimeType = connection.contentType
            }
            responseLength?.set(inputStream.available())
            response.status = 200
        } catch (e: IOException) {
            response.error = CefLoadHandler.ErrorCode.ERR_FILE_NOT_FOUND
            response.statusText = (e.getLocalizedMessage())
            response.status = 404
        }
    }

    override fun readResponse(
        dataOut: ByteArray?,
        bytesToRead: Int,
        bytesRead: IntRef?,
        callback: CefCallback?
    ): Boolean {
        val availableSize = inputStream.available()
        return if (availableSize > 0) {
            val maxBytesToRead = Math.min(availableSize, bytesToRead)
            val realNumberOfReadBytes = inputStream.read(dataOut, 0, maxBytesToRead)
            bytesRead?.set(realNumberOfReadBytes)
            true
        } else {
            inputStream.close()
            false
        }
    }

    override fun close() {
        inputStream.close()
    }
}

object ClosedConnection : ResourceHandlerState() {
    override fun getResponseHeaders(
        response: CefResponse?,
        responseLength: IntRef?,
        redirectUrl: StringRef?
    ) {
        response?.status = 404
    }

    override fun readResponse(
        dataOut: ByteArray?,
        bytesToRead: Int,
        bytesRead: IntRef?,
        callback: CefCallback?
    ): Boolean = false

    override fun close() {}
}
