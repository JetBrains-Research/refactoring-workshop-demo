package org.jetbrains.research.refactoringDemoPlugin.statistics

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.jetbrains.research.refactoringDemoPlugin.util.countLines
import org.jetbrains.research.refactoringDemoPlugin.util.extractJavaClasses
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class MyToolWindowContent(private val project: Project) {
    private val myToolWindowContent: JPanel = JPanel(BorderLayout())

    init {
        createContent()
    }

    private fun createContent() {
        val tableModel = DefaultTableModel()
        val table = JBTable(tableModel)
        tableModel.addColumn("Class Name")
        tableModel.addColumn("Field Count")
        tableModel.addColumn("Method Count")
        tableModel.addColumn("Lines Of Code")
        tableModel.addColumn("Number Of Children")

        val entries: HashMap<String, ClassStatistics> = calculateStatisticsForClasses()
        entries.forEach { e ->
            tableModel.addRow(
                arrayOf(
                    e.key,
                    e.value.fieldCount,
                    e.value.methodCount,
                    e.value.loc,
                    e.value.subclassCount,
                )
            )
        }
        val scrollPane = JBScrollPane(table)
        myToolWindowContent.add(scrollPane, BorderLayout.CENTER)
    }

    private fun calculateStatisticsForClasses(): HashMap<String, ClassStatistics> {
        val results: HashMap<String, ClassStatistics> = hashMapOf()
        val classes = extractJavaClasses(project)
        classes.forEach { clazz ->
            if (clazz.qualifiedName != null) {
                val statistics =
                    ClassStatistics(
                        clazz.fields.size,
                        clazz.methods.size,
                        countLines(clazz.text),
                        clazz.children.size
                    )
                results[clazz.qualifiedName!!] = statistics
            }
        }
        return results
    }

    fun getContent(): JPanel {
        return myToolWindowContent
    }
}
