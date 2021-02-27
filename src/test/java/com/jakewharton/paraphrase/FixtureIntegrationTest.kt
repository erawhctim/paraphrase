package com.jakewharton.paraphrase

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.*
import org.junit.Test
import java.io.File
import java.lang.management.ManagementFactory

// TODO: parameterize this
class FixtureIntegrationTest {
    
    
    @Test
    fun `new simple fixture compiles successfully`() {
//        val fixture = File("src/test/fixtures/simple")
        val fixture = File("src/test/fixtures/new")

        assertWithMessage("Fixture 'simple' does not exist")
            .that(fixture.exists())
            .isTrue()
        
        assertWithMessage("Fixture 'simple' is not a directory")
            .that(fixture.isDirectory)
            .isTrue()
        
        val result = GradleRunner.create()
            .apply {
                withArguments(listOf("clean", "check"))
                withProjectDir(fixture)
                withGradleVersion(GradleVersion.current().version)
                withPluginClasspath()
                forwardOutput()
                // Enables debug mode when `--debug-jvm` is passed to Gradle
                // https://gradle-community.slack.com/archives/CA7UM03V3/p1612641895194900?thread_ts=1612572503.188500&cid=CA7UM03V3
                withDebug(ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0)
            }
            .build()
        
//        println(result.output)
//        println("tasks executed: ${result.tasks}")
        
        assertThat(result.output)
            .contains("BUILD SUCCESSFUL")
    }
}