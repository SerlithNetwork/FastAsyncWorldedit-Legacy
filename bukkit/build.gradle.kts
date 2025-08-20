repositories {
    flatDir {
        dirs("lib")
    }
}

dependencies {
    compileOnly(project(":core"))
    implementation(project(":core"))

    compileOnly("com.sk89q:worldguard:6.0.0-SNAPSHOT")
    compileOnly("com.destroystokyo.paper:paper-api:1.12-R0.1-SNAPSHOT") {
        exclude(group = "net.md-5")
    }
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_12:1.12.1")
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_11:1.11")
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_10:1.10")
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_9:1.9.4")
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_8:1.8.8")
    compileOnly("org.bukkit.craftbukkit:Craftbukkit_1_7:1.7.10")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.ryanhamshire:GriefPrevention:11.5.2")
    compileOnly("net.jzx7:regios:5.9.9")
    compileOnly("com.bekvon.bukkit.residence:Residence:4.5._13.1")
    compileOnly("com.palmergames.bukkit:towny:0.84.0.9")
    compileOnly("com.worldcretornica:plotme_core:0.16.3")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.1.5")
    compileOnly("com.sk89q.worldedit:worldedit-core:6.1.4-SNAPSHOT")
    compileOnly("com.thevoxelbox.voxelsniper:voxelsniper:5.171.0")
    compileOnly("net.dmulloy2:ProtocolLib:5.3.0")
    compileOnly("com.wasteofplastic:askyblock:3.0.9.4")
    compileOnly("org.primesoft:BlocksHub:2.0")
    compileOnly("org.inventivetalent:mapmanager:1.7.2-SNAPSHOT") {
        isTransitive = false
    }

    testCompileOnly("junit:junit:4.13.1")
}

// delete(rootProject.file("target"))

tasks.processResources {
    from("src/main/resources") {
        val props = mapOf("version" to project.parent!!.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.shadowJar {
    dependencies {
        include(dependency("com.github.luben:zstd-jni:1.1.1"))
        include(dependency("co.aikar:fastutil-lite:1.0"))
        include(dependency(":core"))
    }
    archiveFileName.set("${parent!!.name}-${project.name}-${parent!!.version}.jar")
    // destinationDirectory.set(rootProject.file("target"))
    relocate("com.google.gson", "com.sk89q.worldedit.internal.gson")
}

tasks.build {
    dependsOn("shadowJar")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")
}
