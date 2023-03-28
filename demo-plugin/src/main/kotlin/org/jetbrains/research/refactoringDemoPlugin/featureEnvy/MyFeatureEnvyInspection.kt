package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog
import com.siyeh.ig.psiutils.LibraryUtil
import org.jetbrains.research.refactoringDemoPlugin.DemoPluginBundle

/**
 * Walks through methods in the file, check if a method uses entities of another class more than those of enclosing one,
 * and suggest Move Method refactoring.
 */
class MyFeatureEnvyInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = FeatureEnvyInspectionVisitor(holder)

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
                            MoveMethodFix(clazz)
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
    class MoveMethodFix(@FileModifier.SafeFieldForPreview private val destinationClass: PsiClass) : LocalQuickFix {
        override fun getFamilyName() = DemoPluginBundle.message("quick.fix.move.method.name")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val methodToMove = descriptor.psiElement
            require(methodToMove is PsiMethod) { "Move method quick fix must be called only on a method" }
            moveInstanceMethod(methodToMove, destinationClass)
        }

        private fun moveInstanceMethod(methodToMove: PsiMethod, targetClass: PsiClass) {
            val available = ApplicationManager.getApplication().runReadAction<Array<PsiVariable>> {
                getAvailableVariables(methodToMove, targetClass)
            }
            if (available.isEmpty()) {
                return
            }
            ApplicationManager.getApplication().invokeLater {
                val dialog = MoveInstanceMethodDialog(methodToMove, available)
                dialog.title = DemoPluginBundle.message("quick.fix.move.method.dialog.title") + methodToMove.name
                dialog.show()
            }
        }
    }
}

/*
    Concatenates two arrays and filters the elements by the condition.
 */
private fun concatFiltered(
    array1: Array<out PsiVariable>,
    array2: Array<out PsiVariable>,
    condition: (v: PsiVariable) -> Boolean
): Array<PsiVariable> {
    val filteredList = arrayListOf<PsiVariable>()
    array1.filterTo(filteredList, condition)
    array2.filterTo(filteredList, condition)
    return filteredList.toTypedArray()
}

/*
    Searches for variables (fields or parameters) of the type the method is supposed to be moved to.
 */
internal fun getAvailableVariables(method: PsiMethod, target: PsiClass): Array<PsiVariable> {
    val psiClass = method.containingClass
    val parameters: Array<out PsiParameter> = method.parameterList.parameters
    val fields: Array<out PsiField> = psiClass!!.fields
    return concatFiltered(
        parameters,
        fields
    ) { v -> v.type is PsiClassType && target == (v.type as PsiClassType).resolve() }
}
