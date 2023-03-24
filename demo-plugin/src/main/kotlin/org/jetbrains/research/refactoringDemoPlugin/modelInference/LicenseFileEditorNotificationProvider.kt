package org.jetbrains.research.refactoringDemoPlugin.modelInference

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.License
import java.util.function.Function
import javax.swing.JComponent

class LicenseFileEditorNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        ModuleUtilCore.findModuleForFile(file, project) ?: return null
        val licenseFileNamePattern = Regex(
            "(LICENSE.*|LEGAL.*|COPYING.*|COPYLEFT.*|COPYRIGHT.*|UNLICENSE.*|MIT.*|BSD.*|GPL.*|LGPL.*|APACHE.*)(\\.txt|\\.md|\\.html)?",
            RegexOption.IGNORE_CASE
        )
        if (!licenseFileNamePattern.matches(file.name)) {
            return null
        }
        return Function {
            val licenseNotificationPanel = EditorNotificationPanel()
            val licenseDocument: Document = ReadAction.compute<Document, Throwable> {
                FileDocumentManager.getInstance().getDocument(file)!!
            }
            val licenseDetector = LicenseDetector()
            val license: License? = runBlocking {
                licenseDetector.detectLicense(licenseDocument.text)
            }
            val licenseName = license?.name ?: "unknown"
            licenseNotificationPanel.text = "The module license is $licenseName"
            licenseNotificationPanel
        }
    }
}
