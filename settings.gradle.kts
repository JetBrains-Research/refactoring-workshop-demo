rootProject.name = "refactoring-workshop-demo"

include(
    "demo-plugin",
    "demo-cli"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")

    }
}
