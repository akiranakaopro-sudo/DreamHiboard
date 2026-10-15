plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Collect deps: gradlew collectToOfflineMaven -PofflineMavenRepo=D:/Android/maven_repo_hiboard
apply(from = "offline-maven.gradle")
