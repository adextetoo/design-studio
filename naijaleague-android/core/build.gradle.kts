// The part of this product that is hard to get right, compiled and tested on
// a plain JVM.
//
// `rules` is the scoring engine, `data` the researched NPFL clubs and squads,
// and `catalogue` the operator-console reader and its provenance-gated merge.
// None of the three imports an Android class — that was true before this
// module existed and is what made extracting it possible rather than a
// refactor. What was NOT true is that you could run their tests without the
// Android SDK: they lived in `:app`, so `testDebugUnitTest` built them through
// AGP and a contributor with no SDK could not execute the one part of the
// codebase that carries the brand's whole argument.
//
//     ./gradlew -p core test
//
// The Android build gets these classes through includeBuild("core") in the
// root settings file, so there is one copy of the source and one set of tests.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Matched to the dependency `:app` declares. Gradle substitutes the module for
// this build when the coordinates line up, so both have to agree.
group = "ng.naijaleague"
version = "1.0.0"

kotlin {
    compilerOptions {
        // JVM 11, the same target app/build.gradle.kts sets for the Android
        // compilation. A newer target here would produce class files the
        // Android build cannot read.
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    testLogging {
        // A count at the end, so a green run says how green.
        events("passed", "skipped", "failed")
    }
}
