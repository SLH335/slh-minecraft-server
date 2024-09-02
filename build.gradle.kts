plugins {
    id("com.gradleup.shadow") version "8.3.0"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "1.4.20"
}

group = "xyz.hafemann.slhserver"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.minestom:minestom-snapshots:a521c4e7cd")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.jetbrains.exposed:exposed-core:0.54.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.54.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.54.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.TogAr2:MinestomPvP:c985370512")
    implementation("com.charleskorn.kaml:kaml:0.61.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "xyz.hafemann.server.MainKt"
        }
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}
