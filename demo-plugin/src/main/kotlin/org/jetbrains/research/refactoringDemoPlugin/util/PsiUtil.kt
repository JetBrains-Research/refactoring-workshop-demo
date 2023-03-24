package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.KtFile

/*
    Searches for variables (fields or parameters) of the type the method is supposed to be moved to.
 */
fun getAvailableVariables(method: PsiMethod, target: PsiClass): Array<PsiVariable> {
    val psiClass = method.containingClass
    val parameters: Array<out PsiParameter> = method.parameterList.parameters
    val fields: Array<out PsiField> = psiClass!!.fields
    return concatFiltered(
        parameters,
        fields
    ) { v -> v.type is PsiClassType && target == (v.type as PsiClassType).resolve() }
}

/*
    Concatenates two arrays and filters the elements by the condition.
 */
fun concatFiltered(
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
    Extracts all Kotlin and Java classes from the project.
 */
fun Project.extractKotlinAndJavaClasses(): MutableList<PsiClass> {
    val classes = mutableListOf<PsiClass>()
    this.guessProjectDir()?.let { projectDir ->
        ProjectFileIndex.getInstance(this).iterateContentUnderDirectory(
            projectDir,
            {
                when (val psiFile = PsiManager.getInstance(this).findFile(it)) {
                    is PsiJavaFile -> classes.addAll(psiFile.classes)
                    is KtFile -> classes.addAll(psiFile.classes)
                }
                true
            },
            { it.extension == "java" || it.extension == "kt" }
        )
    }
    return classes
}
