plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

//group = "me.baldo3000"
version = "1.0"

dependencies {
//    testImplementation(kotlin("test"))
    implementation(project(":common"))
}

//tasks.test {
//    useJUnitPlatform()
//}

application {
    // Define the Fully Qualified Name for the application main class
    mainClass = "me.baldo3000.server.ServerKt"
}