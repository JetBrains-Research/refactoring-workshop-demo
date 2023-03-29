group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":demo-core"))
}

dependencies {
    implementation("io.kinference:inference-core-jvm:0.2.12") {
        exclude("org.slf4j")
    }
}
