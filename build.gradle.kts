plugins {
    java
    id("com.gradleup.shadow") version "8.3.1"
}

// delete("target")

group = "com.boydti.fawe"
version = "1.0.0-legacy-6.1.7.2" // FAWE version - Edition - WE version

subprojects {
    apply(plugin = "java")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "com.gradleup.shadow")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.dmulloy2.net/repository/public")
        maven("https://repo.destroystokyo.com/repository/maven-public//")
        maven("https://ci.athion.net/plugin/repository/tools/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.maven.apache.org/maven2")
        maven("https://ci.frostcast.net/plugin/repository/everything")
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo.inventivetalent.org/content/groups/public/")
        maven("https://store.ttyh.ru/libraries/")
        maven("https://maven.elmakers.com/repository/")
        maven("https://ci.ender.zone/plugin/repository/everything/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public")
    }

    tasks.compileJava {
        options.compilerArgs.add("-parameters")
    }

    tasks.register<Javadoc>("aggregatedJavadocs") {
        title = "${project.name} $version API"
        description = "Generate javadocs from all child projects as if it was a single project"
        group = "Documentation"

        options.destinationDirectory = file("./docs/javadoc")

        delete {
            file("./docs")
        }

        subprojects.forEach { p ->
            p.tasks.withType(Javadoc::class).forEach { d ->
                source += d.source
                classpath += d.classpath
                excludes += d.excludes
                includes += d.includes
            }
        }

    }

}
