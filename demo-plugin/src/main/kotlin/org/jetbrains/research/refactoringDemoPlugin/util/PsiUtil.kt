package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiVariable
import java.util.Arrays
import java.util.Objects
import java.util.stream.Stream

fun getAvailableVariables(method: PsiMethod, target: PsiClass): Array<PsiVariable> {
    val psiClass = method.containingClass
    val parameters: Stream<PsiVariable> = Arrays.stream(method.parameterList.parameters)
    val fields: Stream<PsiVariable> = if (psiClass == null) Stream.empty() else Arrays.stream(psiClass.fields)
    return Stream.concat(parameters, fields)
        .filter(Objects::nonNull)
        .filter { p -> p.type is PsiClassType && target == (p.type as PsiClassType).resolve() }
        .toArray() as Array<PsiVariable>
}
