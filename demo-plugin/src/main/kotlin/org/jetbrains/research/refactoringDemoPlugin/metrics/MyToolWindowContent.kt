package org.jetbrains.research.refactoringDemoPlugin.metrics

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.jetbrains.research.refactoringDemoPlugin.util.extractClasses
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
        tableModel.addColumn("Field Number")
        tableModel.addColumn("Method Number")
        tableModel.addColumn("Lines Of Code")
        tableModel.addColumn("Number Of Children")
        tableModel.addColumn("Lack Of Cohesion Of Methods")

        val entries: HashMap<String, ClassStatistics> = calculateStatisticsForClasses()
        entries.forEach { e ->
            tableModel.addRow(
                arrayOf(
                    e.key,
                    e.value.fieldsNumber,
                    e.value.methodNumber,
                    e.value.loc,
                    e.value.noc,
                    e.value.lcom
                )
            )
        }
        val scrollPane = JBScrollPane(table)
        myToolWindowContent.add(scrollPane, BorderLayout.CENTER)
    }

    private fun calculateStatisticsForClasses(): HashMap<String, ClassStatistics> {
        val results: HashMap<String, ClassStatistics> = hashMapOf()
        val classes = extractClasses(project)
        classes.forEach { clazz ->
            //TODO: implement LOC, NOC, LCOM metrics calculation
            val statistics = ClassStatistics(clazz.fields.size, clazz.methods.size, 0, 0, 0)
            results[clazz.qualifiedName!!] = statistics
        }
        return results
    }

    fun getContent(): JPanel {
        return myToolWindowContent
    }
}
