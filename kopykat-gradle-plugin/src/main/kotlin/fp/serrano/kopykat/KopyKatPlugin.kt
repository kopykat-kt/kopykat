package fp.serrano.kopykat

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaModule
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

abstract class KopyKatPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val kkSettings = createKopyKatSettings(target)

        if (kkSettings.applyKspPlugin.getOrElse(false)) {
            target.pluginManager.apply("com.google.devtools.ksp")
        }

        configureKsp(target, kkSettings)
    }

    private fun createKopyKatSettings(target: Project): KopyKatSettings {
        return target.extensions.create<KopyKatSettings>(KOPYKAT_EXTENSION_NAME).apply {
            applyKspPlugin.convention(true)

            mutableCopy.convention(true)
            copyMap.convention(true)
            hierarchyCopy.convention(true)
        }
    }

    private fun configureKsp(target: Project, kkSettings: KopyKatSettings) {
        target.plugins.withType<KspGradleSubplugin>().configureEach {

            target.configureSourceSets()

            target.configurations.matching { it.name == "ksp" }.configureEach {
                defaultDependencies {
                    add(target.dependencies.create("com.github.kopykat-kt.kopykat:ksp:1.0-rc1"))
                }
            }

            target.extensions.configure<KspExtension> {
                arg("mutableCopy", "${kkSettings.mutableCopy.getOrElse(false)}")
                arg("copyMap", "${kkSettings.copyMap.getOrElse(false)}")
                arg("hierarchyCopy", "${kkSettings.hierarchyCopy.getOrElse(false)}")
            }
        }
    }

    /**
     * Apply the configuration from https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code
     */
    private fun Project.configureSourceSets() {

        val kspGeneratedMain = "build/generated/ksp/main/kotlin"
        val kspGeneratedTest = "build/generated/ksp/main/test"

        plugins.withType<KotlinBasePlugin>().configureEach {
            kotlinExtension.apply {
                sourceSets.matching { it.name == "main" }.configureEach {
                    kotlin.srcDirs(kspGeneratedMain)
                }
                sourceSets.matching { it.name == "test" }.configureEach {
                    kotlin.srcDirs(kspGeneratedTest)
                }
            }
        }

        plugins.withType<IdeaPlugin>().configureEach {
            extensions.configure<IdeaModel> {
                extensions.configure<IdeaModule> {
                    sourceDirs.plusAssign(file(kspGeneratedMain)) // or tasks["kspKotlin"].destination
                    generatedSourceDirs.plusAssign(file(kspGeneratedMain))

                    testSourceDirs.plusAssign(file(kspGeneratedTest))
                    generatedSourceDirs.plusAssign(file(kspGeneratedTest))

                }
            }
        }
    }

    companion object {
        const val KOPYKAT_EXTENSION_NAME = "kopykat"
    }
}