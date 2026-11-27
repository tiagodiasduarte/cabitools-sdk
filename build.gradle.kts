// Top-level build file where you can add configuration options common to all sub-projects/modules.
import kotlinx.validation.api.dump
import kotlinx.validation.api.filterOutNonPublic
import kotlinx.validation.api.loadApiFromJvmClasses

buildscript {
    dependencies {
        // Reads compiled classes and Kotlin metadata to dump a module's public API (see checkNoOpApi).
        classpath(libs.asm)
        classpath(libs.asm.tree)
        classpath(libs.binary.compatibility.validator)
        classpath(libs.kotlin.metadata.jvm)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

/**
 * Fails when :cabitools-sdk-application and :cabitools-sdk-application-no-op expose different public APIs, so host code that compiles
 * against one always compiles against the other. `internal` declarations are ignored.
 */
@CacheableTask
abstract class CheckNoOpApiTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sdkClasses: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val noOpClasses: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @TaskAction
    fun check() {
        val sdkApi = publicApi(sdkClasses)
        val noOpApi = publicApi(noOpClasses)
        val dir = reportDir.get().asFile
        dir.resolve("application.api").writeText(sdkApi)
        dir.resolve("application-no-op.api").writeText(noOpApi)
        if (sdkApi != noOpApi) {
            val sdkLines = sdkApi.lines().toSet()
            val noOpLines = noOpApi.lines().toSet()
            val onlySdk = (sdkLines - noOpLines).joinToString("\n") { "  - $it" }
            val onlyNoOp = (noOpLines - sdkLines).joinToString("\n") { "  + $it" }
            throw GradleException(
                "Public APIs of :cabitools-sdk-application and :cabitools-sdk-application-no-op differ (dumps in $dir).\n" +
                    "Only in :cabitools-sdk-application:\n$onlySdk\nOnly in :cabitools-sdk-application-no-op:\n$onlyNoOp",
            )
        }
    }

    private fun publicApi(classes: FileCollection): String {
        val classFiles = classes.asFileTree.matching { include("**/*.class") }.files.sortedBy { it.path }
        val dump = classFiles.asSequence()
            .map { it.readBytes().inputStream() }
            .loadApiFromJvmClasses()
            .filterOutNonPublic()
            .dump(StringBuilder())
            .toString()
        // The Compose compiler adds public-looking members that no host code can use:
        // ComposableSingletons classes and `$stable` fields. :cabitools-sdk-application-no-op has no Compose.
        return dump.trim().split("\n\n")
            .filterNot { it.contains("ComposableSingletons") }
            .joinToString("\n\n") { block -> block.lines().filterNot { "field \$stable" in it }.joinToString("\n") }
    }
}

tasks.register<CheckNoOpApiTask>("checkNoOpApi") {
    group = "verification"
    description = "Checks that :cabitools-sdk-application and :cabitools-sdk-application-no-op have the same public API."
    sdkClasses.from(provider { project(":cabitools-sdk-application").tasks.named("compileDebugKotlin").get().outputs.files })
    noOpClasses.from(provider { project(":cabitools-sdk-application-no-op").tasks.named("compileDebugKotlin").get().outputs.files })
    reportDir.set(layout.buildDirectory.dir("reports/noOpApi"))
}
