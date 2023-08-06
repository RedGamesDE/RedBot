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
        baseImage.set("eclipse-temurin:17-jre")
        maintainer.set("RedGames")
        ports.set(emptyList())
        images.set(setOf(
            "registry.redgames.de/redbot:$version",
            "registry.redgames.de/redbot:latest"
        ))
    }

    registryCredentials {
        url.set("https://registry.redgames.de/")
        username.set(providers.environmentVariable("REDGAMES_REGISTRY_USERNAME"))
        password.set(providers.environmentVariable("REDGAMES_REGISTRY_PASSWORD"))
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
