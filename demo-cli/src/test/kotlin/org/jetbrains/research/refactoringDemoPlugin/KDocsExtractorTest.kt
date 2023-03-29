package org.jetbrains.research.refactoringDemoPlugin

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.research.refactoringDemoPlugin.util.extractElementsOfType

class KDocsExtractorTest : BasePlatformTestCase() {
    fun testKDocExtractor() {
        val psi = myFixture.configureByText(
            "dummy.kt",
            """
            /**
             * My KDoc 1
             */
            fun foo1() = run { }

            class A {
                /**
                 * My KDoc 2
                 */
                fun foo2() = run { }
            }
            """.trimIndent()
        )
        val expected = mapOf(
            "foo1" to
                """
            /**
            * My KDoc 1
            */
                """.trimIndent(),
            "foo2" to """
            /**
            * My KDoc 2
            */
            """.trimIndent()
        )
        val kDocs = psi.extractElementsOfType(PsiComment::class.java).filter { it.parent is KtFunction }
        assert(kDocs.size == 2) { "The expected size of the kDocs list is 2" }
        val actual = kDocs.associate { (it.parent as PsiNamedElement).name to it.text }
        assert(expected.all { (f, d) -> actual[f] != null && actual[f]!!.trimEachLine() == d.trimEachLine() })
    }

    companion object {
        private fun String.trimEachLine() = this.lines().joinToString(System.lineSeparator()) { it.trimIndent() }
    }
}
