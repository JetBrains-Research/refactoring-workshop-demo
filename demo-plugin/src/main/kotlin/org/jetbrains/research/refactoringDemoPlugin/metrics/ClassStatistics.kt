package org.jetbrains.research.refactoringDemoPlugin.metrics

data class ClassStatistics(
    // number of methods in the class
    val methodNumber: Int,
    // number of fields in the class
    val fieldsNumber: Int,
    // lines of code inside the class
    val loc: Int,
    // lack of cohesion of methods
    val lcom: Int,
    // number of children (subclasses)
    val noc: Int
)
