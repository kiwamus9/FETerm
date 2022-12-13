plugins {
    kotlin("js") version "1.7.21"
}

group = "jp.kiwamus9"
version = "1.0-SNAPSHOT"

repositories {
    jcenter() // kotlinx-nodejs
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
//    implementation(npm("electron", "^19.0.7"))
//    implementation(npm("electron-devtools-installer","^3.2.0"))
//    implementation(npm("electron-reloader", "^1.2.3"))
}

kotlin {
    js(IR) {
        sourceSets["main"].apply {
            kotlin.srcDirs(
                layout.projectDirectory.dir("../kotlinCommon/common"),
                layout.projectDirectory.dir("../kotlinCommon/node")
            )
        }
        binaries.executable()
        nodejs {
            compilations["main"].packageJson {
                customField("hello", mapOf("one" to 1, "two" to 2))
//                customField("main","kotlin/main.js")
            }
        }
        useCommonJs()
    }
}
