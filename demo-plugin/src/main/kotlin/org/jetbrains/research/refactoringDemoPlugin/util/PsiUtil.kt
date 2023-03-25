package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
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
        parameters, fields
    ) { v -> v.type is PsiClassType && target == (v.type as PsiClassType).resolve() }
}

/*
    Concatenates two arrays and filters the elements by the condition.
 */
fun concatFiltered(
    array1: Array<out PsiVariable>, array2: Array<out PsiVariable>, condition: (v: PsiVariable) -> Boolean
): Array<PsiVariable> {
    val filteredList = arrayListOf<PsiVariable>()
    array1.filterTo(filteredList, condition)
    array2.filterTo(filteredList, condition)
    return filteredList.toTypedArray()
}

/*
    Extracts all Kotlin and Java classes from the project.
 */
fun Project.extractKotlinAndJavaClasses(): List<PsiClass> =
    this.extractPsiFiles { it.extension == "java" || it.extension == "kt" }.mapNotNull { file ->
        when (file) {
            is PsiJavaFile -> file.classes.toList()
            is KtFile -> file.classes.toList()
            else -> null
        }
    }.flatten()

fun Project.extractPsiFiles(filePredicate: (VirtualFile) -> Boolean): MutableSet<PsiFile> {
    val projectPsiFiles = mutableSetOf<PsiFile>()
    val projectRootManager = ProjectRootManager.getInstance(this)
    val psiManager = PsiManager.getInstance(this)

    projectRootManager.contentRoots.mapNotNull { root ->
        VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
            if (!filePredicate(virtualFile) || virtualFile.canonicalPath == null) {
                return@iterateChildrenRecursively true
            }
            val psi = psiManager.findFile(virtualFile) ?: return@iterateChildrenRecursively true
            projectPsiFiles.add(psi)
        }
    }
    return projectPsiFiles
}