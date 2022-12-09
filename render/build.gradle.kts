plugins {
    kotlin("js") version "1.7.21"
}

group = "jp.kiwamus9"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

val fritz2Version = "1.0-RC1"

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.457")
    implementation("dev.fritz2:core:$fritz2Version")
    implementation(npm("html-to-image", "1.10.8"))
    implementation(npm("xterm","^5.0.0"))

}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                // cssSupport.enabled = true
                cssSupport {
                    enabled = true
                }
            }
        }
    }
}
