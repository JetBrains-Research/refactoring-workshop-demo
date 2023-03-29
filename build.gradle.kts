group = "org.jetbrains.research.refactoringDemoPlugin"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.8.10" apply true
    id("org.jetbrains.intellij") version "1.13.2" apply true
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply true
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    repositories {
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/ki/maven")
    }

    intellij {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        downloadSources.set(properties("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    ktlint {
        enableExperimentalRules.set(true)
        disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
        filter {
            exclude("**/resources/**")
        }
    }

    val jvmVersion = "17"

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = jvmVersion
            }
        }

        withType<JavaCompile> {
            sourceCompatibility = jvmVersion
            targetCompatibility = jvmVersion
        }

        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}
