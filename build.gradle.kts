import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

plugins {
    java
}

version = "1.0.0"

sourceSets {
    main {
        java.srcDirs("src")
    }
}

repositories {
    mavenCentral()

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/dependencies.jar")
        }
        metadataSources {
            artifact()
        }
    }

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/master/[revision].jar")
        }
        metadataSources {
            artifact()
        }
    }

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/jabel.jar")
        }
        metadataSources {
            artifact()
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_17
}

val mindustryVersion = "v157"
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRoot: String? = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
val mindustryClientJar: String? = providers.gradleProperty("mindustryClientJar").orNull

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("--release", "8"))
    }
}

dependencies {
    compileOnly("Anuken:Mindustry:$mindustryVersion")
    annotationProcessor("Anuken:jabel:v1.0.0")
}

tasks.register("jarAndroid") {
    dependsOn("jar")

    doLast {
        val sdkDir = sdkRoot?.let(::File)
        if (sdkDir == null || !sdkDir.exists()) {
            throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        }

        val platformRoot = File(sdkDir, "platforms").listFiles()
            ?.sortedDescending()
            ?.firstOrNull { File(it, "android.jar").exists() }
            ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")

        val classpath = (
            configurations.compileClasspath.get().files +
            configurations.runtimeClasspath.get().files +
            File(platformRoot, "android.jar")
        )

        val d8 = if (isWindows) "d8.bat" else "d8"

        val command = mutableListOf(d8)
        classpath.forEach {
            command.add("--classpath")
            command.add(it.path)
        }
        command.addAll(listOf("--min-api", "14", "--output", "${project.name}Android.jar", "${project.name}Desktop.jar"))

        val process = ProcessBuilder(command)
            .directory(file("build/libs"))
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("d8 exited with code $exitCode.")
        }
    }
}

tasks.jar {
    archiveFileName.set("${project.name}Desktop.jar")

    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })

    from(projectDir) {
        include("mod.hjson")
        include("sprites/**")
        include("bundles/**")
    }
}

tasks.register<Jar>("deploy") {
    dependsOn("jarAndroid")
    dependsOn("jar")
    archiveFileName.set("${project.name}.jar")

    from({
        listOf(
            zipTree("build/libs/${project.name}Desktop.jar"),
            zipTree("build/libs/${project.name}Android.jar")
        )
    })

    doLast {
        delete("build/libs/${project.name}Android.jar")
    }
}

tasks.register("runClient") {
    dependsOn("jar")
    group = "mindustry"
    description = "Runs Mindustry with this mod installed into the local run directory."

    doLast {
        val clientJar = mindustryClientJar?.let(::File)
            ?: throw GradleException("Set -PmindustryClientJar=/path/to/Mindustry.jar")

        if (!clientJar.isFile) {
            throw GradleException("Mindustry client jar does not exist: ${clientJar.absolutePath}")
        }

        val runDir = layout.projectDirectory.dir("run").asFile
        val modsDir = File(runDir, "mods")

        copy {
            from(tasks.jar.flatMap { it.archiveFile })
            into(modsDir)
        }

        val process = ProcessBuilder(
            "java",
            "-Dmindustry.data.dir=${runDir.absolutePath}",
            "-Dnodiscord=true",
            "-jar",
            clientJar.absolutePath
        )
            .directory(runDir)
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("Mindustry exited with code $exitCode.")
        }
    }
}
