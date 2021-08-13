package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiField

/*
    Searches for variables (fields or parameters) of the type the method is supposed to be moved to.
 */
fun getAvailableVariables(method: PsiMethod, target: PsiClass): Array<PsiVariable> {
    val psiClass = method.containingClass
    val parameters: Array<out PsiParameter> = method.parameterList.parameters
    val fields: Array<out PsiField> = psiClass!!.fields
    return concat(parameters, fields) { v -> v.type is PsiClassType && target == (v.type as PsiClassType).resolve() }
}

/*
    Concatenates two arrays and filters the elements by the condition.
 */
fun concat(
    array1: Array<out PsiVariable>,
    array2: Array<out PsiVariable>,
    condition: (v: PsiVariable) -> Boolean
): Array<PsiVariable> {
    var resultArray = arrayOf<PsiVariable>()
    for (element in array1) {
        if (condition(element))
            resultArray += element
    }

    for (element in array2) {
        if (condition(element))
            resultArray += element
    }
    return resultArray
}
