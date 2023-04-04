package org.jetbrains.research.refactoringDemoPlugin.jcef

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.jcef.JBCefApp
import org.jetbrains.research.refactoringDemoPlugin.statistics.StatisticsService

class JcefWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val jcefWindow = project.getService(JcefWindowService::class.java).jcefWindow
        val jComponent = toolWindow.component
        setupButtonEvent(jcefWindow, project)
        jComponent.parent.add(jcefWindow.jComponent)
    }

    private fun StringBuilder.addTableRow(vararg data: String) {
        append("""newRow = document.createElement("tr");""")
        for (value in data) {
            append("""newTh = document.createElement("th");""")
            append("""newTh.appendChild(document.createTextNode("$value"));""")
            append("""newRow.appendChild(newTh);""")
        }
        append("""document.getElementById("stattable").appendChild(newRow);""")
    }

    fun setupButtonEvent(jcefWindow: JcefWindow, project: Project) {
        jcefWindow.executeJavascript(
            """
            table = document.getElementById("stattable");
            
            const but = document.getElementById('button');
            but.onclick = function() {
                clicked = "button was clicked";
            """,
            """}""",
            "clicked"
        ) {
            val statistics = project.getService(StatisticsService::class.java).getStatistics(project)

            val jsCode = StringBuilder()

            for ((fqName, stat) in statistics) {
                jsCode.addTableRow(stat.fileName, fqName, stat.methodCount.toString(), stat.loc.toString())
            }

            jcefWindow.windowBrowser.cefBrowser.mainFrame.executeJavaScript(
                jsCode.toString(),
                jcefWindow.windowBrowser.cefBrowser.url,
                0
            )

            null
        }
    }

    override fun isApplicable(project: Project): Boolean {
        return super.isApplicable(project) && JBCefApp.isSupported()
    }
}
