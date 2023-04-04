package org.jetbrains.research.refactoringDemoPlugin.jcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefSchemeHandlerFactory
import org.cef.network.CefRequest

class CustomSchemeHandlerFactory : CefSchemeHandlerFactory {
    override fun create(
        cefBrowser: CefBrowser,
        cefFrame: CefFrame,
        s: String,
        cefRequest: CefRequest
    ) = CustomResourceHandler()
}
