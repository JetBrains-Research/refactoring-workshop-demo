rootProject.name = "refactoring-workshop-demo"

include(
    "demo-plugin",
    "demo-cli",
    "demo-core"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
    }
}
