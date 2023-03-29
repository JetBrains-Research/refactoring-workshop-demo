package org.jetbrains.research.refactoringDemoPlugin.featureEnvy

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class MyFeatureEnvyInspectionTest : LightJavaCodeInsightFixtureTestCase() {
    override fun getTestDataPath() = MyFeatureEnvyInspectionTest::class.java.getResource("testData")?.path
        ?: error("Resource featureEnvy does not exist")

    fun testSimple() {
        doTest()
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MyFeatureEnvyInspection())
    }

    private fun doTest() {
        myFixture.testInspection("simple", LocalInspectionToolWrapper(MyFeatureEnvyInspection()))
    }
}
