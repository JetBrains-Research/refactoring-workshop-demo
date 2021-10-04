rootProject.name = "refactoring-workshop-demo"

include(
    "demo-plugin",
    "demo-cli"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(java.net.URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
    }
}
