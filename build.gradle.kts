plugins {
    idea
    java
    id("com.bmuschko.docker-java-application") version "9.3.2"
}

group = "nexus.slime"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.theholywaffle:teamspeak3-api:1.3.1")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("com.google.code.gson:gson:2.10.1")
}

docker {
    javaApplication {
        baseImage = "eclipse-temurin:17-jre"
        maintainer = "Slime Nexus"
        ports = emptyList()
        images = setOf(
            "registry.slime.nexus/redbot:$version",
            "registry.slime.nexus/redbot:latest"
        )
    }

    registryCredentials {
        url = "https://registry.slime.nexus/"
        username = providers.environmentVariable("SLIME_NEXUS_REGISTRY_USERNAME")
        password = providers.environmentVariable("SLIME_NEXUS_REGISTRY_PASSWORD")
    }
}

tasks {
    jar {
        manifest {
            attributes("Main-Class" to "nexus.slime.redbot.Bot")
        }
    }

    dockerSyncBuildContext {
        from("config")
    }

    dockerCreateDockerfile {
        copyFile("*.json", "config/")
    }
}
