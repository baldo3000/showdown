// kotlin
// File: `server/build.gradle.kts.txt.txt.txt`

val appName: String by project

plugins {
    id("application")
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
    mainClass.set("me.baldo3000.showdown.server.ServerKt")
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
}
