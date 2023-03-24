package org.jetbrains.research.refactoringDemoPlugin.modelInference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.License

class LicenseFileEditorNotificationProvider : EditorNotifications.Provider<EditorNotificationPanel>() {
    companion object {
        private val KEY: Key<EditorNotificationPanel> = Key.create("LicenseFile")
    }

    override fun getKey(): Key<EditorNotificationPanel> = KEY

    override fun createNotificationPanel(
        file: VirtualFile,
        fileEditor: FileEditor,
        project: Project,
    ): EditorNotificationPanel? {
        ModuleUtilCore.findModuleForFile(file, project) ?: return null
        val licenseFileNamePattern = Regex(
            "(LICENSE.*|LEGAL.*|COPYING.*|COPYLEFT.*|COPYRIGHT.*|UNLICENSE.*|" +
                "MIT.*|BSD.*|GPL.*|LGPL.*|APACHE.*)(\\.txt|\\.md|\\.html)?",
            RegexOption.IGNORE_CASE
        )
        if (!licenseFileNamePattern.matches(file.name)) {
            return null
        }
        val licenseNotificationPanel = EditorNotificationPanel()
        val licenseDocument: Document = ReadAction.compute<Document, Throwable> {
            FileDocumentManager.getInstance().getDocument(file)!!
        }
        val licenseDetector = LicenseDetector()
        val license: License? = ApplicationManager.getApplication().runReadAction<License?> {
            licenseDetector.detectLicense(licenseDocument.text)
        }
//        val license = licenseDetector.detectLicense(licenseDocument.text)
        val licenseName = license?.name ?: "unknown"
        licenseNotificationPanel.text = "The module license is $licenseName"
        return licenseNotificationPanel
    }
}
