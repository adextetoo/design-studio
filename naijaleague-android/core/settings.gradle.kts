// A BUILD OF ITS OWN, not a subproject, and that is the entire point.
//
// As an ordinary `include(":core")` module this would still be useless for the
// job it exists to do. Gradle configures every project in a build before it
// runs anything in one, so `./gradlew :core:test` would configure `:app`, the
// Android Gradle Plugin would look for an SDK, and a machine without one would
// be told:
//
//     SDK location not found. Define a valid SDK location with an ANDROID_HOME
//     environment variable or by setting the sdk.dir path in your project's
//     local.properties file.
//
// — while running tests that do not import a single Android class. Making this
// a separate build is what actually keeps AGP out of the way:
//
//     ./gradlew -p core test        # no Android SDK, no AGP, no emulator
//
// The root build pulls it back in with includeBuild("core"), so `:app` still
// compiles against these sources and `assembleDebug` is unaffected.
rootProject.name = "core"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        // NO google() ON PURPOSE. Nothing here may depend on AndroidX, and a
        // repository that cannot serve it is a cheaper guard than a review
        // comment: an import that reaches for androidx fails to resolve rather
        // than quietly compiling and dragging the SDK back into this build.
        mavenCentral()
    }

    // The same version catalog the Android build uses, read from its file, so
    // Kotlin and the test libraries cannot drift between the two builds.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
