package fp.serrano.kopykat

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

abstract class KopyKatPlugin : Plugin<Project> {

    private val logger: Logger = Logging.getLogger(this::class.java)

    override fun apply(target: Project) {

        val kkSettings = createKopyKatSettings(target)

        target.plugins.withType<KspGradleSubplugin> {
            target.afterEvaluate {
                // normally afterEvaluate should be avoided, but it's necessary here so the KopyKat settings can be
                // applied *after* the user has configured the KopyKat settings in a build script.
                configureKsp(target, kkSettings)
            }
        }
    }

    private fun createKopyKatSettings(target: Project): KopyKatSettings {
        return target.extensions.create<KopyKatSettings>(KOPYKAT_EXTENSION_NAME).apply {
            configureGeneratedSourceSets.convention(true)

            mutableCopy.convention(true)
            copyMap.convention(true)
            hierarchyCopy.convention(true)
        }
    }

    private fun configureKsp(target: Project, kkSettings: KopyKatSettings) {

        if (kkSettings.configureGeneratedSourceSets.orNull == true) {
            target.configureSourceSets()
        }

        target.extensions.configure<KspExtension> {
            kkSettings.mutableCopy.orNull?.let { value ->
                logger.debug("setting kopykat.mutableCopy to $value")
                arg("mutableCopy", "$value")
            }
            kkSettings.hierarchyCopy.orNull?.let { value ->
                logger.debug("setting kopykat.hierarchyCopy to $value")
                arg("hierarchyCopy", "$value")
            }
            kkSettings.copyMap.orNull?.let { value ->
                logger.debug("setting kopykat.copyMap to $value")
                arg("copyMap", "$value")
            }
        }
    }

    /**
     * Apply the configuration from https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code
     */
    private fun Project.configureSourceSets() {
        logger.debug("configuring KSP generated source sets")

        val kspGeneratedMain = "build/generated/ksp/main/kotlin"
        val kspGeneratedTest = "build/generated/ksp/main/test"

        plugins.withType<KotlinBasePlugin> {
            kotlinExtension.apply {
                sourceSets.matching { it.name == "main" }.configureEach {
                    kotlin.srcDirs(kspGeneratedMain)
                }
                sourceSets.matching { it.name == "test" }.configureEach {
                    kotlin.srcDirs(kspGeneratedTest)
                }
            }
        }

        plugins.withType<IdeaPlugin> {
            extensions.configure<IdeaModel> {
                module {
                    sourceDirs.plusAssign(file(kspGeneratedMain))
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
