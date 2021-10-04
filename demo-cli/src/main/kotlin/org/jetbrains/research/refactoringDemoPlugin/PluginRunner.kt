package org.jetbrains.research.refactoringDemoPlugin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
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
    private val output by argument(help = "Output directory").file(canBeFile = true)

    /**
     * Walks through files in the project, extracts all methods in each file
     * and saves the method name and the corresponding JavaDoc to the output file.
     */
    override fun run() {
        val preprocessor = getKotlinJavaPreprocessorManager(null)
        val repositoryOpener = getKotlinJavaRepositoryOpener()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val fileWriter = FileWriter(output, true)
        val datasetItems: ArrayList<DatasetItem> = arrayListOf()

        preprocessor.preprocessDatasetInplace(input.toPath().toFile())
        getSubdirectories(input.toPath()).forEach { repositoryRoot ->
            repositoryOpener.openRepository(repositoryRoot.toFile()) { project ->
                val files = extractJavaFiles(project)
                files.forEach { file ->
                    file.classes.forEach { clazz ->
                        clazz.methods.forEach { method ->
                            val comment = method.docComment?.text ?: ""
                            datasetItems.add(DatasetItem(method.name, comment))
                        }
                    }
                }
            }
        }
        val resultJson = gson.toJson(datasetItems)
        fileWriter.write(resultJson)
        exitProcess(0)
    }

    /**
     * Extracts Java files in the project.
     */
    private fun extractJavaFiles(project: Project): MutableList<PsiJavaFile> {
        val javaFiles: MutableList<PsiJavaFile> = java.util.ArrayList()
        val projectDir = project.guessProjectDir()
        if (projectDir != null) {
            ProjectFileIndex.SERVICE.getInstance(project)
                .iterateContentUnderDirectory(
                    projectDir,
                    { file: VirtualFile ->
                        val psiFile = PsiManager.getInstance(project).findFile(file)
                        if (psiFile is PsiJavaFile) {
                            javaFiles.add(psiFile)
                        }
                        true
                    },
                    createFileFilter()
                )
        }
        return javaFiles
    }

    private fun createFileFilter(): VirtualFileFilter {
        return VirtualFileFilter { file: VirtualFile ->
            file.name.endsWith(".java")
        }
    }

    private fun getSubdirectories(path: Path): List<Path> {
        return Files.walk(path, 1)
            .filter { Files.isDirectory(it) && !it.equals(path) }
            .toList()
    }

    data class DatasetItem(val methodName: String, val javaDoc: String)
}
