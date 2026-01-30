dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.ktor.network)
    implementation(libs.logback.classic)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

tasks.test {
    useJUnitPlatform()
}
