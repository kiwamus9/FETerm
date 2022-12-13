
var electronPath = layout.projectDirectory.dir("electron")

// root project
tasks.register("buildDevelop") {
    dependsOn(":main:copyDevelopOutputs",
        ":preload:copyDevelopOutputs",
        ":render:copyDevelopOutputs")
}
tasks.register("buildProduction") {
    dependsOn(":main:copyProductionOutputs",
        ":preload:copyProductionOutputs",
        ":render:copyProductionOutputs")
}

project("main") {
    tasks.register<Copy>("copyDevelopOutputs") {
        dependsOn("developmentExecutableCompileSync")
        from(layout.buildDirectory.dir("compileSync/main/developmentExecutable/kotlin"))
        into(electronPath.dir("mainProcess"))
    }
    tasks.register<Copy>("copyProductionOutputs") {
        dependsOn("productionExecutableCompileSync")
        from(layout.buildDirectory.dir("compileSync/main/productionExecutable/kotlin"))
        into(electronPath.dir("mainProcess"))
    }
}

project("preload") {
    tasks.register<Copy>("copyDevelopOutputs") {
        dependsOn("developmentExecutableCompileSync")
        from(layout.buildDirectory.dir("compileSync/main/developmentExecutable/kotlin"))
        into(electronPath.dir("preloadProcess"))
    }
    tasks.register<Copy>("copyProductionOutputs") {
        dependsOn("productionExecutableCompileSync")
        from(layout.buildDirectory.dir("compileSync/main/productionExecutable/kotlin"))
        into(electronPath.dir("preloadProcess"))
    }
}

project("render") {
    tasks.register<Copy>("copyDevelopOutputs") {
        dependsOn("browserDevelopmentWebpack")
        from(layout.buildDirectory.dir("developmentExecutable"))
        into(electronPath)
    }
    tasks.register<Copy>("copyProductionOutputs") {
        dependsOn("browserProductionWebpack")
        from(layout.buildDirectory.dir("distributions"))
        into(electronPath)
    }
}
