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

tasks {
    runIde {
        val input: String? by project
        val output: String? by project
        args = listOfNotNull("DemoPluginCLI", input, output)
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "2g"
    }

    register("runDemoPluginCLI") {
        dependsOn(runIde)
    }
}
