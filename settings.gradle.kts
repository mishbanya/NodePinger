pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://artifacts.consensys.net/public/maven/maven/") }
        maven { url = uri("https://dl.cloudsmith.io/public/consensys/maven-teku/maven/") }
        maven { url = uri("https://hyperledger.jfrog.io/artifactory/besu-maven/") }
    }
}

rootProject.name = "NodePinger"
include(":app")
 