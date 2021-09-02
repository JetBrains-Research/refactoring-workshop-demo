package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable

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
    Extracts all Java classes from the project.
 */
fun extractJavaClasses(project: Project): MutableList<PsiClass> {
    val classes: MutableList<PsiClass> = ArrayList()

    ProjectFileIndex.SERVICE.getInstance(project).iterateContent { file: VirtualFile? ->
        val psiFile = PsiManager.getInstance(project).findFile(file!!)
        if (psiFile is PsiJavaFile && !psiFile.isDirectory() &&
            "JAVA" == psiFile.getFileType().name
        ) {
            classes.addAll(psiFile.classes)
        }
        true
    }
    return classes
}

/*
    Calculates the number of lines in the text.
 */
fun countLines(classText: String) = classText.toCharArray().count { it == '\n' || it == '\r' }
