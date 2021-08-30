package org.jetbrains.research.refactoringDemoPlugin.metrics

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.refactoringDemoPlugin.util.extractClasses
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class MyToolWindowContent(private val project: Project) {
    private val myToolWindowContent: JPanel = JPanel(BorderLayout())

    init {
        createContent()
    }

    private fun createContent() {
        val model = DefaultTableModel()
        val table = JTable(model)
        model.addColumn("Class name")
        model.addColumn("Field number")
        model.addColumn("Method number")
        model.addColumn("Lines Of Code")
        model.addColumn("Number Of Children")
        model.addColumn("Lack Of Cohesion Of Methods")

        val rows: HashMap<String, ClassStatistics> = calculateStatisticsForClasses()
        rows.forEach { r ->
            model.addRow(
                arrayOf(
                    r.value.fieldsNumber,
                    r.value.methodNumber,
                    r.value.loc,
                    r.value.noc,
                    r.value.lcom
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
