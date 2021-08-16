group = rootProject.group
version = rootProject.version

open class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val runner: String? by project

    init {
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "20g"
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<IOCliTask>("runDemoPlugin") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            runner
        )
    }
}
