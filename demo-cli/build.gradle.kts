group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("com.google.code.gson:gson:2.7")
    val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
    dependencies {
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "main"
            }
        }
    }
}

open class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val runner: String? by project
    val input: String? by project
    val output: String? by project

    init {
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "2g"
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<IOCliTask>("runDemoPluginCLI") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            runner,
            input?.let { it },
            output?.let { it }
        )
    }
}
