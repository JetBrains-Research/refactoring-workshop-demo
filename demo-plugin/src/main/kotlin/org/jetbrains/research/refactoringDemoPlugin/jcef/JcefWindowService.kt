package org.jetbrains.research.refactoringDemoPlugin.jcef

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class JcefWindowService : Disposable {
    val jcefWindow = JcefWindow(this)
    override fun dispose() {
        logger<Project>().info("Jcef window service was disposed")
    }

    companion object {
        val pluginResourcesDomain = "fakedomain"
    }
}
