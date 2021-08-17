package org.jetbrains.research.refactoringDemoPlugin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.google.gson.GsonBuilder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager

class PluginRunner : ApplicationStarter {
    override fun getCommandName(): String {
        return "DemoPluginCLI"
    }

    override fun main(args: Array<String>) {
        JavaDocExtractor().main(args.drop(1))
    }
}

class JavaDocExtractor : CliktCommand() {
    private val projectPath by argument(help = "Path to the project").file(mustExist = true, canBeFile = false)
    private val outputPath by argument(help = "Output directory").file(canBeFile = false)

    private val fileTypeName = "JAVA"

    /**
     * Walks through files in the project, extracts all methods in each file
     * and saves the method and the corresponding JavaDoc to the output file.
     */
    override fun run() {
        val project = ProjectUtil.openOrImport(projectPath.path, null, true) ?: return
        val gson = GsonBuilder().setPrettyPrinting().create()
        val files = extractFiles(project)
        files.forEach { file ->
            file.classes.forEach { c ->
                c.methods.forEach { m ->
                    val datasetItem = DatasetItem(m.name, m.docComment.toString())
                    val json = gson.toJson(datasetItem)
                    outputPath.writeText(json)
                }
            }
        }
    }

    /**
     * Extracts Java files in the project.
     */
    private fun extractFiles(project: Project): List<PsiJavaFile> {
        val javaFiles: MutableList<PsiJavaFile> = ArrayList()
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent { file: VirtualFile? ->
            val psiFile = PsiManager.getInstance(project).findFile(file!!)
            if (psiFile is PsiJavaFile && !psiFile.isDirectory()
                && fileTypeName == psiFile.getFileType().name
            ) {
                javaFiles.add(psiFile)
            }
            true
        }
        return javaFiles
    }

    data class DatasetItem(val methodName: String, val javaDoc: String)
}