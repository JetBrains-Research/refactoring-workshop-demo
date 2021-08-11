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

class MyFeatureEnvyInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return FeatureEnvyInspectionVisitor(holder)
    }

    class FeatureEnvyInspectionVisitor(private val holder: ProblemsHolder?) : PsiElementVisitor() {
        private val minimumAccessesNumber = 3

        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (element is PsiMethod) {
                val method: PsiMethod = element
                val currentClass = method.containingClass
                val visitor = ClassAccessVisitor(currentClass!!)
                visitor.visitElement(method)

                val accessedClasses = visitor.accessedClasses
                accessedClasses.forEach { accessedClass ->
                    if (accessedClass.value >= minimumAccessesNumber) {
                        if (canMoveInstanceMethod(method, accessedClass.key)) {
                            holder?.registerProblem(method, "", MoveMethodFix(method, accessedClass.key))
                        }
                    }
                }
            }
        }

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
            moveInstanceMethod(methodToMove, destinationClass)
        }

        private fun moveInstanceMethod(methodToMove: PsiMethod, targetClass: PsiClass) {
            val available = getAvailableVariables(methodToMove, targetClass)
            check(available.isNotEmpty()) { "Cannot move method" }
            val dialog = MoveInstanceMethodDialog(methodToMove, available)
            dialog.title = "Move Method " + methodToMove.name
            ApplicationManager.getApplication().invokeAndWait { dialog.show() }
        }
    }
}
