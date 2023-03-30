package org.jetbrains.research.refactoringDemoPlugin.jcef

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.executeJavaScriptAsync
import com.jetbrains.rd.util.first
import com.jetbrains.rd.util.firstOrNull
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import org.jetbrains.research.refactoringDemoPlugin.statistics.ClassStatistics
import org.jetbrains.research.refactoringDemoPlugin.statistics.StatisticsService
import java.awt.BorderLayout

class JcefWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val jcefWindow = project.getService(JcefWindowService::class.java).jcefWindow
        val jComponent = toolWindow.component
        setupButtonEvent(jcefWindow, project)
        jComponent.parent.add(jcefWindow.jComponent)
    }

    private val LOG = getLogger(JcefWindow::class)

    private fun StringBuilder.addTableRow(vararg data: String) {
        append("""newRow = document.createElement("tr");""")
        for (value in data) {
            LOG.warn { "??? newTh.appendChild(document.createTextNode($value));" }
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
            var count = 0;
            
            const but = document.getElementById('button');
            but.onclick = function() {
                clicked1 = "button was clicked";
                count += 1;
            """,
            """}""",
            "clicked1"
        ) {
            LOG.warn { "Event: $it" }
            val statistics = project.getService(StatisticsService::class.java).getStatistics(project)

            LOG.warn { "Stats count: ${statistics.size}" }

            val jsCode = StringBuilder()

            jsCode.append("""smth = document.createElement("span");""")
            jsCode.append("""smth.appendChild(document.createTextNode("sanity check"));""")
            jsCode.append("""document.getElementById("root").appendChild(smth);""")

            for ((fqName, stat) in statistics) {
                jsCode.addTableRow(stat.fileName, fqName, stat.methodCount.toString(), stat.loc.toString())
            }

            jcefWindow.windowBrowser.cefBrowser.mainFrame.executeJavaScript(
                jsCode.toString(),
                jcefWindow.windowBrowser.cefBrowser.url,
                0
            )

            LOG.warn { "Executed?" }
            null
        }
    }

    override fun isApplicable(project: Project): Boolean {
        return super.isApplicable(project) && JBCefApp.isSupported()
    }
}