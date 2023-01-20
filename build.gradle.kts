group = "org.jetbrains.research.refactoringDemoPlugin"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.7.21" apply true
    id("org.jetbrains.intellij") version "1.1.3" apply true
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
        jcenter()
        maven("https://packages.jetbrains.team/maven/p/ki/maven")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("io.kinference:inference:0.1.4")
        implementation("org.jetbrains.research:plugin-utilities-core:1.0")
        implementation("org.jetbrains.research:plugin-utilities-test:1.0")
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
        filter {
            exclude("**/resources/**")
        }
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
            jvmArgs = listOf("-Djdk.module.illegalAccess.silent=true")
        }
    }
}
