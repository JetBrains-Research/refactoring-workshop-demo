package org.jetbrains.research.refactoringDemoPlugin.statistics

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.research.refactoringDemoPlugin.util.extractKotlinAndJavaClasses

@Service(Service.Level.APP)
class StatisticsService : Disposable {
    fun getStatistics(project: Project) =
        ApplicationManager.getApplication().runReadAction<Map<String, ClassStatistics>> {
            project.extractKotlinAndJavaClasses().mapNotNull { psi ->
                psi.qualifiedName?.let {
                    it to ClassStatistics(
                        psi.containingFile.name,
                        psi.methods.size,
                        psi.text.countLines()
                    )
                }
            }.toMap()
        }

    override fun dispose() {
    }
}
