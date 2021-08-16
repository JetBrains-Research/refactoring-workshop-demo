package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog
import org.jetbrains.research.refactoringDemoPlugin.util.getAvailableVariables

/**
 * Walks through methods in the file, check if a method uses entities of another class more than those of enclosing one,
 * and suggest Move Method refactoring.
 */
class MyFeatureEnvyInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return FeatureEnvyInspectionVisitor(holder)
    }

    class FeatureEnvyInspectionVisitor(private val holder: ProblemsHolder?) : PsiElementVisitor() {
        private val minAccessCount = 3

        override fun visitElement(element: PsiElement) {
            if (element is PsiMethod) {
                val method: PsiMethod = element
                val currentClass = method.containingClass ?: return
                val visitor = ClassAccessVisitor(currentClass)
                visitor.visitElement(method)

                val accessedClasses = visitor.accessedClasses
                accessedClasses.forEach { (clazz, accessCount) ->
                    if (accessCount >= minAccessCount) {
                        if (canMoveInstanceMethod(method, clazz)) {
                            holder?.registerProblem(
                                method,
                                "Method uses methods of another class more than those of the enclosing class.",
                                MoveMethodFix(method, clazz)
                            )
                        }
                    }
                }
            }
        }

        /**
         * Checks if a method could be moved to the target class.
         * Returns true if a type of the target class occurs in fields' or parameters' types.
         */
        private fun canMoveInstanceMethod(method: PsiMethod, target: PsiClass): Boolean {
            val available = getAvailableVariables(method, target)
            return available.isNotEmpty()
        }
    }

    class MoveMethodFix(private val methodToMove: PsiMethod, private val destinationClass: PsiClass) : LocalQuickFix {
        private val quickFixName = "Move method to a more related class"

        override fun getFamilyName(): String {
            return quickFixName
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            ApplicationManager.getApplication().runReadAction {
                moveInstanceMethod(methodToMove, destinationClass)
            }
        }

        private fun moveInstanceMethod(methodToMove: PsiMethod, targetClass: PsiClass) {
            val available = getAvailableVariables(methodToMove, targetClass)
            if (available.isEmpty()) {
                return
            }
            val dialog = MoveInstanceMethodDialog(methodToMove, available)
            dialog.title = "Move Method " + methodToMove.name
            ApplicationManager.getApplication().invokeAndWait { dialog.show() }
        }
    }
}
