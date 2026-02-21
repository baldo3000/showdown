plugins {
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.ktor.network)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
