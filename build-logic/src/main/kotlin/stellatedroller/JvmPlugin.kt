package stellatedroller

import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

class JvmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply(plugin = "java")
            apply(plugin = "org.cadixdev.licenser")
            configure<JavaPluginExtension> {
                toolchain.languageVersion.set(JavaLanguageVersion.of(17))
                withJavadocJar()
                withSourcesJar()
            }
            tasks.withType<JavaCompile> {
                options.compilerArgs.add("-parameters")
            }
            tasks.named<Test>("test") {
                useJUnitPlatform()
            }
            tasks.named<Javadoc>("javadoc") {
                options.encoding = "UTF-8"
                (options as StandardJavadocDocletOptions).apply {
                    // addBooleanOption("Werror", true)
                    tags(
                        "apiNote:a:API Note:",
                        "implSpec:a:Implementation Requirements:",
                        "implNote:a:Implementation Note:"
                    )
                }
                // Disable up-to-date check, it is wrong for javadoc with -Werror
                outputs.upToDateWhen { false }
            }
            configure<LicenseExtension> {
                exclude {
                    it.file.startsWith(project.buildDir)
                }
                header(rootProject.file("HEADER.txt"))
                (this as ExtensionAware).extra.apply {
                    for (key in listOf("organization", "url")) {
                        set(key, rootProject.property(key))
                    }
                }
            }
        }
    }
}
