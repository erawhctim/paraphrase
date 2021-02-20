package com.jakewharton.paraphrase

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.functional.GradleRunnerFactory
import org.junit.Test
import java.io.File

// TODO: parameterize this
class FixtureIntegrationTest {
    
    @Test
    fun `simple fixture compiles successfully`() {
        val fixture = File("src/test/fixtures/simple")
        
        assertWithMessage("Fixture 'simple' does not exist")
            .that(fixture.exists())
            .isTrue()
    
        assertWithMessage("Fixture 'simple' is not a directory")
            .that(fixture.isDirectory)
            .isTrue()
        
        val runner = GradleRunnerFactory.create()
        runner.directory = fixture
        runner.arguments.addAll(listOf("clean", "check", "--stacktrace"))
        
        val result = runner.run()
        assertThat(result.standardOutput)
            .contains("BUILD SUCCESSFUL")
    }
}