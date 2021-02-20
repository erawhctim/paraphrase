package com.jakewharton.paraphrase

import com.android.ide.common.resources.ResourceItem
import com.android.ide.common.symbols.IdProvider
import com.android.ide.common.symbols.IdProvider.Companion
import com.android.ide.common.symbols.SymbolTable
import com.android.ide.common.symbols.parseResourceFile
import com.android.ide.common.symbols.parseResourceSourceSetDirectory
import com.android.resources.ResourceFolderType.VALUES
import com.android.resources.ResourceType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

abstract class GenerateParaphraseClassesTask: DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>
    
    @get:Incremental
    @get:InputDirectory
    abstract val resDir: DirectoryProperty
    
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty
    
    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(
            if (inputChanges.isIncremental) {
                "Executing incrementally"
            } else {
                "Executing non-incrementally"
            }
        )
        
        inputChanges.getFileChanges(resDir).forEach { change ->
            println("task InputChange: ${change.normalizedPath} - ${change.changeType}")
            
            val items = ValueResourceParser.parse(resDir.get().asFile)
            
        }
    }
}

object ValueResourceParser {
    
    fun parse(directory: File): List<ResourceItem> {
        val symbolTable = parseResourceSourceSetDirectory(
            directory = directory,
            idProvider = IdProvider.sequential(),
            platformAttrSymbols = null
        )
        
        val stringSymbols = symbolTable.getSymbolByResourceType(ResourceType.STRING)
        
        println("Found ${stringSymbols.size} String symbols")
        stringSymbols.forEach { println("\t${it.name}") }
        
        return emptyList()
    }
}
