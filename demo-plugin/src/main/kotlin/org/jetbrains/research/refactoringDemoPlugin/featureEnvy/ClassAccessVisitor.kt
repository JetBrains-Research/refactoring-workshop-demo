package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethodCallExpression
import com.siyeh.ig.psiutils.LibraryUtil

/**
 * Extracts all accesses to other classes excluding library ones within the method.
 */
class ClassAccessVisitor(private val currentClass: PsiClass) : JavaRecursiveElementVisitor() {
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
