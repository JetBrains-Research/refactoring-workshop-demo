package org.jetbrains.research.refactoringDemoPlugin.statistics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.jetbrains.research.refactoringDemoPlugin.DemoPluginBundle
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class StatisticsToolWindowContent(private val project: Project) {
    val content: JPanel = JPanel(BorderLayout())

    init {
        createContent()
    }

    private fun createContent() {
        val tableModel = DefaultTableModel()
        val table = JBTable(tableModel)
        val columns = listOf(
            "tool.window.class.file",
            "tool.window.class.name",
            "tool.window.method.count",
            "tool.window.lines.of.code"
        )
        columns.forEach { tableModel.addColumn(DemoPluginBundle.message(it)) }

        val statistics = project.getService(StatisticsService::class.java).getStatistics(project)

        ApplicationManager.getApplication().invokeAndWait {
            statistics.forEach { (fqName, stat) ->
                tableModel.addRow(
                    arrayOf(
                        stat.fileName,
                        fqName,
                        stat.methodCount,
                        stat.loc
                    )
                )
            }
            content.add(JBScrollPane(table), BorderLayout.CENTER)
        }
    }
}
