plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Dependência padrão para Kotlin
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(21)
}

