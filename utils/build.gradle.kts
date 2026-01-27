dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.ktor.network)
    implementation(libs.logback.classic)
}

tasks.test {
    useJUnitPlatform()
}
