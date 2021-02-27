import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.TaskTriggersConfig

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    `kotlin-dsl`
    groovy
    maven
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.13.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.0"
}

/*
 * Some classpath manipulation magic in order for the test fixtures to be able to find/see the actual plugin,
 * otherwise the GradleRunner will fail with "plugin com.whatever not found"
 */
val testSourceSet = sourceSets["test"].apply {
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

tasks.named<Test>("test").configure {
    testClassesDirs += testSourceSet.output.classesDirs
    classpath += testSourceSet.runtimeClasspath
}

///*
// * Extra configuration needed to enable the text fixtures classpath to see the plugin's classpath
// *
// * See:
// * https://stackoverflow.com/a/54393454/1452741
// */
//val fixIdeaPluginClasspath = task("fixIdeaPluginClasspath") {
//    doFirst {
//        tasks.withType<PluginUnderTestMetadata>().configureEach {
//            val ideaClassesPath = project.buildDir.toPath().resolveSibling("out").resolve("production")
//            val newClasspath = project.files().map { it.toPath() }.toMutableList()
//            newClasspath.add(0, ideaClassesPath)
//            pluginClasspath.setFrom(newClasspath)
//        }
//    }
//}
//
//tasks.withType<PluginUnderTestMetadata>().configureEach {
//    mustRunAfter(fixIdeaPluginClasspath)
//    pluginClasspath.from(configurations.compileOnly)
//}
//
//
//// Below is from:
//// https://github.com/JetBrains/gradle-idea-ext-plugin/issues/76#issuecomment-483373014
//fun Project.idea(block: IdeaModel.() -> Unit) =
//    (this as ExtensionAware).extensions.configure("idea", block)
//
//fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
//    (this@settings as ExtensionAware).extensions.configure(block)
//
//fun ProjectSettings.taskTriggers(block: TaskTriggersConfig.() -> Unit) =
//    (this@taskTriggers as ExtensionAware).extensions.configure("taskTriggers", block)
//
//rootProject.idea {
//    project {
//        settings {
//            taskTriggers {
//                val pluginUnderTestMetadata = tasks.withType<PluginUnderTestMetadata>().first()
//                beforeBuild(fixIdeaPluginClasspath, pluginUnderTestMetadata)
//            }
//        }
//    }
//}


repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    compileOnly(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:3.6.1")
    testRuntimeOnly("com.android.tools.build:gradle:3.6.1")
    implementation("com.android.tools:sdk-common:27.0.2")
    implementation("com.squareup:javawriter:2.4.0")
    implementation("com.google.guava:guava:16.0.1")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:1.0.1")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("paraphrase") {
            id = "com.jakewharton.paraphrase"
            implementationClass = "com.jakewharton.paraphrase.ParaphrasePlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

group = "com.jakewharton.paraphrase"
version = "1.0.0-SNAPSHOT"

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.4"
}