plugins {
    idea
    java
    id("com.bmuschko.docker-java-application") version "9.3.2"
}

group = "de.redgames"
version = "1.1"

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
        maintainer = "RedGames"
        ports = emptyList()
        images = setOf(
            "registry.redgames.de/redbot:$version",
            "registry.redgames.de/redbot:latest"
        )
    }

    registryCredentials {
        url = "https://registry.redgames.de/"
        username = providers.environmentVariable("REDGAMES_REGISTRY_USERNAME")
        password = providers.environmentVariable("REDGAMES_REGISTRY_PASSWORD")
    }
}

tasks {
    jar {
        manifest {
            attributes("Main-Class" to "de.redgames.redbot.Bot")
        }
    }

    dockerSyncBuildContext {
        from("config")
    }

    dockerCreateDockerfile {
        copyFile("*.json", "config/")
    }
}
