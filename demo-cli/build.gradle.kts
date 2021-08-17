group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("com.google.code.gson:gson:2.7")
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
        maxHeapSize = "20g"
    }

    register("runDemoPluginCLI") {
        dependsOn(runIde)
    }
}
