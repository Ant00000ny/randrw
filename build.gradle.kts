plugins {
    kotlin("multiplatform") version "1.9.20-RC2"
}

group = "fun.fantasea.randrw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    // target windows
    val nativeTarget = mingwX64("native")

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
                implementation("com.soywiz.korlibs.krypto:krypto:2.2.0")
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}
