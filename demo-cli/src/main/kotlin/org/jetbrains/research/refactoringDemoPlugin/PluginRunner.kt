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
import java.io.FileWriter
import kotlin.system.exitProcess

class PluginRunner : ApplicationStarter {
    override fun getCommandName(): String {
        return "DemoPluginCLI"
    }

    override fun main(args: Array<String>) {
        JavaDocExtractor().main(args.drop(1))
    }
}

class JavaDocExtractor : CliktCommand() {
    private val input by argument(help = "Path to the project").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)

    private val fileTypeName = "JAVA"

    /**
     * Walks through files in the project, extracts all methods in each file
     * and saves the method name and the corresponding JavaDoc to the output file.
     */
    override fun run() {
        val project = ProjectUtil.openOrImport(input.path, null, true) ?: return
        val gson = GsonBuilder().setPrettyPrinting().create()
        val fileWriter = FileWriter(output, true)
        fileWriter.write("[")
        val files = extractJavaFiles(project)
        files.forEach { file ->
            file.classes.forEach { clazz ->
                clazz.methods.forEach { method ->
                    val comment = method.docComment?.text ?: ""
                    val datasetItem = DatasetItem(method.name, comment)
                    val json = gson.toJson(datasetItem)
                    fileWriter.write("$json, \n")
                }
            }
        }
        fileWriter.write("]")
        exitProcess(0)
    }

    /**
     * Extracts Java files in the project.
     */
    private fun extractJavaFiles(project: Project): List<PsiJavaFile> {
        val javaFiles: MutableList<PsiJavaFile> = ArrayList()
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent { file: VirtualFile? ->
            if (file != null) {
                val psiFile = PsiManager.getInstance(project).findFile(file)
                if (psiFile is PsiJavaFile && !psiFile.isDirectory() && fileTypeName == psiFile.getFileType().name) {
                    javaFiles.add(psiFile)
                }
            }
            true
        }
        return javaFiles
    }

    data class DatasetItem(val methodName: String, val javaDoc: String)
}
