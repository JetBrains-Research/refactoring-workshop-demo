package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog
import com.siyeh.ig.psiutils.LibraryUtil
import org.jetbrains.research.refactoringDemoPlugin.DemoPluginBundle
import org.jetbrains.research.refactoringDemoPlugin.util.getAvailableVariables

/**
 * Walks through methods in the file, check if a method uses entities of another class more than those of enclosing one,
 * and suggest Move Method refactoring.
 */
class MyFeatureEnvyInspection : AbstractBaseJavaLocalInspectionTool() {

    /**
     * Extracts all accesses to other classes excluding library ones within the method.
     */
    private class ClassAccessVisitor(private val currentClass: PsiClass) : JavaRecursiveElementVisitor() {
        val accessCountPerClass: HashMap<PsiClass, Int> = HashMap()

        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            super.visitMethodCallExpression(expression)
            val method = expression.resolveMethod() ?: return
            val calledClass = method.containingClass ?: return

            if (currentClass == calledClass || LibraryUtil.classIsInLibrary(calledClass)) {
                return
            }

            accessCountPerClass.compute(calledClass) { _, count -> (count ?: 0) + 1 }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = FeatureEnvyInspectionVisitor(holder)

    class FeatureEnvyInspectionVisitor(private val holder: ProblemsHolder?) : PsiElementVisitor() {
        companion object {
            private const val MIN_ACCESS_COUNT = 3
        }

        override fun visitElement(element: PsiElement) {
            if (element !is PsiMethod) {
                return
            }
            val currentClass = element.containingClass ?: return
            val visitor = ClassAccessVisitor(currentClass)
            visitor.visitElement(element)

            val accessCounts = visitor.accessCountPerClass
            accessCounts.forEach { (clazz, accessCount) ->
                if (accessCount >= MIN_ACCESS_COUNT) {
                    if (canMoveInstanceMethod(element, clazz)) {
                        holder?.registerProblem(
                            element,
                            DemoPluginBundle.message("problem.holder.move.method.description"),
                            MoveMethodFix(element, clazz)
                        )
                    }
                }
            }
        }

        /**
         * Checks if a method could be moved to the target class.
         * Returns true if a type of the target class occurs in fields' or parameters' types.
         */
        private fun canMoveInstanceMethod(method: PsiMethod, target: PsiClass) =
            getAvailableVariables(method, target).isNotEmpty()
    }

    @Suppress("StatefulEp")
    class MoveMethodFix(private val methodToMove: PsiMethod, private val destinationClass: PsiClass) : LocalQuickFix {

        override fun getFamilyName() = DemoPluginBundle.message("quick.fix.move.method.name")

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
            dialog.title = DemoPluginBundle.message("quick.fix.move.method.dialog.title") + methodToMove.name
            ApplicationManager.getApplication().invokeAndWait { dialog.show() }
        }
    }
}
