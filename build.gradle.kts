import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "tf.tuff"
version = "1.0.0"


repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/everything/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    /*
    implementation("com.github.retrooper:packetevents-spigot:2.11.1")
    compileOnly("com.viaversion:viabackwards:5.3.2")
    compileOnly("com.viaversion:viaversion:5.4.1")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    compileOnly("io.netty:netty-all:4.1.97.Final")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    */

    implementation(libs.packetevents.spigot)
    compileOnly(libs.viabackwards)
    compileOnly(libs.viaversion)
    compileOnly(libs.fastutil)
    implementation(libs.jackson.databind)
    compileOnly(libs.netty.all)
    implementation(libs.java.websocket)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok) 
}


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"

    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "name" to project.name
            )
        }
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")

        relocate("com.github.retrooper.packetevents", "tf.tuff.packetevents")
        relocate("io.github.retrooper.packetevents", "tf.tuff.packetevents")
        relocate("com.fasterxml.jackson", "tf.tuff.jackson")
        relocate("org.java_websocket", "tf.tuff.websocket")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("META-INF/versions/**")
        exclude("module-info.class")
    }

    withType<Jar> {
        enabled = false
    }

    named("build") {
        dependsOn(shadowJar)
    }
}