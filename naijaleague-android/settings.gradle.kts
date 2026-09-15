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

rootProject.name = "NaijaLeague Fantasy"
include(":app")

// The scoring engine, the researched data and the console reader, as a build
// of its own so their tests can run on a plain JVM with no Android SDK:
//
//     ./gradlew -p core test
//
// It is includeBuild rather than include(":core") deliberately — see the
// header of core/settings.gradle.kts. `:app` depends on "ng.naijaleague:core"
// and Gradle substitutes this build for it, so there is still one copy of the
// source and assembleDebug is unchanged.
includeBuild("core")
