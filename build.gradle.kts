
plugins {
    id("fabric-loom") version "1.6-SNAPSHOT" // Fabric Loom
    id("io.github.p03w.machete") version "1.1.4" // Build jar compression
    id("me.modmuss50.mod-publish-plugin") version "0.4.5" // Mod publishing

    id("maven-publish") // Maven publishing
    id("java")
}

//////
fun property(name: String): String = project.properties[name].toString()
fun fabricApiModule(name: String, version: String? = null): Dependency {
    if (version == null) {
        return fabricApi.module(name, (project.findProperty("deps.fabricApi") ?: throw IllegalArgumentException("Fabric API version (deps.fabricApi) is not set")).toString())
    }
    return fabricApi.module(name, version)
}
//////
val javaVersion = JavaVersion.forClassVersion(44 + property("mod.java").toInt())
val minecraftVersion = property("mod.minecraft")
val loaderVersion = property("mod.loader")
val parchmentVersion = property("mod.parchment")

val modVersion: String = file("VERSION").readText().trim()
val modGroup = property("mod.group")
val modId = property("mod.id")
val modName = property("mod.name")
val modDescription = property("mod.description")

val group = modGroup
version = "${minecraftVersion}-${modVersion}"
//////

base {
    archivesName = modId
}

repositories {
    maven("https://repo.eclipse.org/content/repositories/tm4e-snapshots/") // TM4E snapshots
    maven("https://maven.parchmentmc.org") // Parchment mappings
    // maven("https://repo.eclipse.org/content/groups/eclipse")
}

loom {
    // Check if the access widener exists
    accessWidenerPath = file("src/main/resources/${modId}.accesswidener").takeIf { it.exists() }
}


dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")

    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()

        if (parchmentVersion.contains(":"))
            // Use exact version
            parchment("org.parchmentmc.data:parchment-${parchmentVersion}@zip")
        else
            // Use minecraft version + given date
            parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${parchmentVersion}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

    // Fabric API
    modImplementation(fabricApiModule("fabric-api-base"))
    modImplementation(fabricApiModule("fabric-resource-loader-v0"))
    modImplementation(fabricApiModule("fabric-command-api-v2"))

    // TM4E
    include(implementation("org.eclipse:org.eclipse.tm4e.core:${property("deps.tm4e")}")) {}
    // Required dependencies for TM4E
    include(implementation("org.jruby.joni:joni:${property("deps.joni")}")) {}
    include(compileOnly("org.jruby.jcodings:jcodings:${property("deps.jcodings")}")) {}
    // include(implementation("org.w3c.css:sac:1.3"))
    // YAML support
    include(implementation("org.snakeyaml:snakeyaml-engine:${property("deps.snakeyaml")}")) {}

    compileOnly("org.eclipse.jdt:org.eclipse.jdt.annotation:${property("deps.jdtAnnotation")}")
}

tasks {
    processResources {
        inputs.property("modLoader", loaderVersion)
        inputs.property("modJava", java.targetCompatibility.majorVersion)
        inputs.property("modName", modName)
        inputs.property("modVersion", modVersion)
        inputs.property("modDescription", modDescription)
        project.findProperty("deps.fabricApi") ?: inputs.property("depsFabricApi", property("deps.fabricApi"))

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand("modLoader" to loaderVersion,
                "modJava" to java.targetCompatibility.majorVersion,
                "modName" to modName,
                "modVersion" to modVersion,
                "modDescription" to modDescription
            )
            project.findProperty("deps.fabricApi") ?: expand("depsFabricApi" to property("deps.fabricApi"))
        }
    }

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}"}
        }
    }

    publishMods {
        file = remapJar.get().archiveFile
        changelog = providers.environmentVariable("CHANGELOG").getOrElse("No changelog provided")
        type = BETA
        displayName = "$modName $modVersion"
        modLoaders.add("fabric")
        dryRun = providers.environmentVariable("CI").getOrNull() == null

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
            projectId = "1032169"
            minecraftVersions.add(minecraftVersion)
            requires("fabric-api")
        }
        modrinth {
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            projectId = "rjsZCeTS"
            minecraftVersions.add(minecraftVersion)
            requires("fabric-api")
        }
    }

    publishing {
        if (System.getenv("MAVEN_URL") != null) {
            publications {
                create<MavenPublication>("jar") {
                    repositories {
                        maven(System.getenv("MAVEN_URL")) {         // Maven repository URL
                            credentials {
                                username=System.getenv("MAVEN_USER")
                                password=System.getenv("MAVEN_PASSWORD")
                            }
                        }
                    }
                    groupId = group
                    artifactId = modId

                    // Includes jar, sources and dependencies
                    from(project.components["java"])
                }
            }
        } else {
            logger.warn("[!] Maven URL is not set, skipped setting up Maven publishing.")
        }
    }

    // Fix machete compression
    getAllTasks(true).forEach {
        for (task in it.value) {
            // All publishing tasks depend on the remapJar task. But also metadata generation.
            if (task.name.startsWith("publish") || task.name == "generateMetadataFileForJarPublication") {
                task.dependsOn("optimizeOutputsOfRemapJar")
            }
        }
    }
}