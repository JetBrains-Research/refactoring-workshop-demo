package org.jetbrains.research.refactoringDemoPlugin

import com.intellij.AbstractBundle
import org.jetbrains.annotations.PropertyKey

object DemoPluginBundle : AbstractBundle("messages.DemoPluginBundle") {
    fun message(
        @PropertyKey(resourceBundle = "messages.DemoPluginBundle") key: String,
        vararg params: Any
    ): String = getMessage(key, *params)
}
