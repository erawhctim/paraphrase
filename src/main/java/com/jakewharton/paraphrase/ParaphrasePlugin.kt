package com.jakewharton.paraphrase

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class ParaphrasePlugin: Plugin<Project> {
    
    override fun apply(project: Project) {
        val hasApp = project.extensions.findByType(AppExtension::class.java) != null
        val hasLib = project.extensions.findByType(LibraryExtension::class.java) != null
        if (!hasApp && !hasLib) {
            throw IllegalStateException("'android' or 'android-library' plugin required.")
        }
        
        val log = project.logger
        val variants: Collection<BaseVariant> = if (hasApp) {
            project.extensions.findByType(AppExtension::class.java)!!.applicationVariants
        } else {
            project.extensions.findByType(LibraryExtension::class.java)!!.libraryVariants
        }
        
        variants.forEach { variant ->
            val outDir = File("${project.buildDir}/paraphrase/${variant.dirName}")
            log.debug("Paraphrase [${variant.name}] outDir: $outDir")
            
            val mergeTask = variant.mergeResourcesProvider.get()
            val buildConfig = variant.generateBuildConfigProvider.get()
            
            val phraseTaskName = "generate${variant.name.capitalize()}Phrase"
            
            val phraseTask = project.tasks.create(phraseTaskName, GenerateParaphraseClassesTask::class.java) { task ->
                task.resDir.set(mergeTask.outputDir)
                task.outputDir.set(outDir)
            }
            
            buildConfig.doLast {
                val packageName = buildConfig.appPackageName
                log.debug("Paraphrase [${variant.name}] packageName: $packageName")
                phraseTask.packageName.set(packageName)
            }
            
            phraseTask.dependsOn(mergeTask, buildConfig)
            
            variant.registerJavaGeneratingTask(phraseTask, outDir)
        }
    }
}