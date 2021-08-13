package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethodCallExpression
import com.siyeh.ig.psiutils.LibraryUtil

/**
 * Extracts all accesses to other classes excluding library ones within the method.
 */
class ClassAccessVisitor(private val currentClass: PsiClass) : JavaRecursiveElementVisitor() {
    var accessedClasses: HashMap<PsiClass, Int> = HashMap()

    override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
        super.visitMethodCallExpression(expression)
        val method = expression.resolveMethod() ?: return
        val calledClass = method.containingClass ?: return

        if (currentClass == calledClass) {
            return
        }

        if (LibraryUtil.classIsInLibrary(calledClass)) {
            return
        }

        if (accessedClasses.contains(calledClass)) {
            val currentCount: Int = accessedClasses[calledClass]!!
            accessedClasses[calledClass] = currentCount + 1
        } else {
            accessedClasses[calledClass] = 1
        }
    }
}
