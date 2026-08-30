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
    }
}

// Typesafe project accessors: projects.core.data вместо project(":core:data")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MoexMonitor"

include(":app")

include(":core:model")
include(":core:common")
include(":core:ui")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:di")

include(":feature:market")
include(":feature:security")
include(":feature:news")
include(":feature:watchlist")
