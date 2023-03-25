package org.jetbrains.research.refactoringDemoPlugin.statistics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.jetbrains.research.refactoringDemoPlugin.DemoPluginBundle
import org.jetbrains.research.refactoringDemoPlugin.util.extractKotlinAndJavaClasses
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class StatisticsToolWindowContent(private val project: Project) {
    val content: JPanel = JPanel(BorderLayout())

    init {
        createContent()
    }

    private fun createContent() {
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                val tableModel = DefaultTableModel()
                val table = JBTable(tableModel)
                val columns = listOf(
                    "tool.window.class.file",
                    "tool.window.class.name",
                    "tool.window.method.count",
                    "tool.window.lines.of.code"
                )
                columns.forEach { tableModel.addColumn(DemoPluginBundle.message(it)) }

                calculateStatisticsForClasses().forEach { e ->
                    tableModel.addRow(
                        arrayOf(
                            e.value.fileName,
                            e.key,
                            e.value.methodCount,
                            e.value.loc
                        )
                    )
                }
                content.add(JBScrollPane(table), BorderLayout.CENTER)
            }
        }
    }

    private fun calculateStatisticsForClasses() = project.extractKotlinAndJavaClasses().mapNotNull { psi ->
        psi.qualifiedName?.let {
            it to ClassStatistics(
                psi.containingFile.name,
                psi.methods.size,
                psi.text.countLines()
            )
        }
    }.toMap()
}