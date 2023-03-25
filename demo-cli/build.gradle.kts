group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.research:plugin-utilities-core:1.0")
    implementation(project(":demo-core"))
}

abstract class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val runner: String? by project

    @get:Input
    val input: String? by project

    @get:Input
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
