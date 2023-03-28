package org.jetbrains.research.refactoringDemoPlugin.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFile

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

fun Project.extractModules(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}

/** Finds [PsiFile] in module by given file extension. */
fun Module.findPsiFilesByExtension(extension: String): List<PsiFile> {
    val psiManager = PsiManager.getInstance(project)
    return FilenameIndex.getAllFilesByExt(project, extension, moduleContentScope)
        .mapNotNull { psiManager.findFile(it) }
        .toList()
}

fun <T : PsiElement> PsiElement.extractElementsOfType(psiElementClass: Class<T>): MutableCollection<T> =
    PsiTreeUtil.collectElementsOfType(this, psiElementClass)
