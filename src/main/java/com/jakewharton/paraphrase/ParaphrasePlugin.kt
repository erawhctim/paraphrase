package com.jakewharton.paraphrase

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.crash.afterEvaluate
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf
import java.io.File

class ParaphrasePlugin: Plugin<Project> {
    
    override fun apply(project: Project) {
        project.pluginManager.withPlugin(ANDROID_APP_PLUGIN) {
            project.logger.debug("Found Android app plugin")

            val appExtension = project.the<AppExtension>()
            appExtension.applicationVariants.forEach { variant ->
                project.logger.debug("AppExtension variant '$variant'")
            }
        }
        project.pluginManager.withPlugin(ANDROID_LIBRARY_PLUGIN) {
            project.logger.debug("Found Android library plugin")

            val appExtension = project.the<LibraryExtension>()
            appExtension.libraryVariants.forEach { variant ->
                project.logger.debug("LibraryExtension variant '$variant'")
            }
        }

        project.plugins.forEach {
            when (it) {
                is AppPlugin -> {
                    val appExtension = project.extensions.getByType(AppExtension::class.java)
                    val variants = appExtension.applicationVariants
                }
                is LibraryPlugin -> {
                    val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
                    val variants = libraryExtension.libraryVariants
                }
            }
        }

//        val appExtension = project.extensions.findByType(AppExtension::class.java)
//        val libraryExtension = project.extensions.findByType(LibraryExtension::class.java)

        // TODO: Figure out how to get an AppExtension and figure out what BaseAppModuleExtension_Decorated is...
        //  https://github.com/gradle/gradle/issues/11338

        // Maybe try converting main plugin build files to kotlin DSL?

        val androidExtension = project.extensions.findByName("android")
        val appExtension = androidExtension as? AppExtension
        val androidLibraryExtension = project.extensions.findByName("library")
        val libraryExtension = androidLibraryExtension as? LibraryExtension

        val hasApp = appExtension != null
        val hasLib = libraryExtension != null

        if (!hasApp && !hasLib) {
            throw IllegalStateException("'android' or 'android-library' plugin required.")
        }

        val variants: Collection<BaseVariant> = if (hasApp) {
            appExtension!!.applicationVariants
        } else {
            libraryExtension!!.libraryVariants
        }

        variants.forEach { variant ->
            registerPhraseTask(project, variant)
        }
    }

    private fun registerPhraseTask(
        project: Project,
        variant: BaseVariant
    ) {
        val outDir = File("${project.buildDir}/paraphrase/${variant.dirName}")
        project.logger.debug("Paraphrase [${variant.name}] outDir: $outDir")

        val mergeTask = variant.mergeResourcesProvider.get()
        val buildConfig = variant.generateBuildConfigProvider.get()

        val phraseTaskName = "generate${variant.name.capitalize()}Phrase"

        val phraseTask = project.tasks.create(phraseTaskName, GenerateParaphraseClassesTask::class.java) {
            resDir.set(mergeTask.outputDir)
            outputDir.set(outDir)
        }

        buildConfig.doLast {
            val packageName = buildConfig.appPackageName
            project.logger.debug("Paraphrase [${variant.name}] packageName: $packageName")
            phraseTask.packageName.set(packageName)
        }

        phraseTask.dependsOn(mergeTask, buildConfig)

        variant.registerJavaGeneratingTask(phraseTask, outDir)
    }
}

inline fun <reified T : Any> Project.the(): T =
    typeOf<T>().let { type ->
        convention.findByType(type)
            ?: convention.findPlugin(T::class.java)
            ?: convention.getByType(type)
    }

inline fun <reified T> typeOf(): TypeOf<T> =
    object : TypeOf<T>() {}

private const val ANDROID_APP_PLUGIN = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"