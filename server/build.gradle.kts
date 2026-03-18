val appName: String by project

plugins {
    id("application")
    alias(libs.plugins.kotlin.plugin.serialization)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("me.baldo3000.showdown.ServerKt")
}

plugins.withId("eclipse") {
    eclipse {
        project {
            name = "$appName-server"
        }
    }
}

dependencies {
    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.ktor.server.test.host)

    implementation(project(":common"))
}
tasks.test {
    useJUnitPlatform()
}

