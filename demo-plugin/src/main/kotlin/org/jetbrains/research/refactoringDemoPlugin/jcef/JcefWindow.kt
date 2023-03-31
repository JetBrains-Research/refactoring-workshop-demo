package org.jetbrains.research.refactoringDemoPlugin.jcef

import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.ui.jcef.JCEFHtmlPanel
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.intellij.lang.annotations.Language
import javax.swing.JComponent


class JcefWindow(service: JcefWindowService) {
    val windowBrowser: JBCefBrowser
    val jComponent: JComponent
        get() = windowBrowser.component

    private val loadHandlers: MutableMap<String, CefLoadHandler> = mutableMapOf()

    init {
        windowBrowser = JBCefBrowser()
        windowBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 100)
        registerAppSchemeHandler()
        windowBrowser.loadURL("http://${JcefWindowService.pluginResourcesDomain}/index.html")
        Disposer.register(service, windowBrowser)
    }

    private fun registerAppSchemeHandler() {
        CefApp.getInstance().registerSchemeHandlerFactory(
            "http",
            JcefWindowService.pluginResourcesDomain,
            CustomSchemeHandlerFactory()
        )
    }

    fun executeJavascript(
        @Language("JavaScript") codeBeforeInject: String = "",
        @Language("JavaScript") codeAfterInject: String = "",
        queryResult: String = "",
        handler: (String) -> JBCefJSQuery.Response?
    ) {
        val jbCefJSQuery = JBCefJSQuery.create(windowBrowser as JBCefBrowserBase)
        Disposer.register(windowBrowser, jbCefJSQuery)
        jbCefJSQuery.addHandler(handler)

        val newLoadHandler = object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                windowBrowser.cefBrowser.executeJavaScript(
                    "${codeBeforeInject.trimIndent()} ${jbCefJSQuery.inject(queryResult)}; ${codeAfterInject.trimIndent()}",
                    windowBrowser.cefBrowser.url,
                    0
                )
                super.onLoadEnd(browser, frame, httpStatusCode)
            }
        }

        loadHandlers[queryResult]?.let {
            windowBrowser.jbCefClient.removeLoadHandler(it, windowBrowser.cefBrowser)
        }

        windowBrowser.jbCefClient.addLoadHandler(newLoadHandler, windowBrowser.cefBrowser)

        loadHandlers[queryResult] = newLoadHandler
    }
}