package fp.serrano.kopykat

import org.gradle.api.provider.Property

interface KopyKatSettings {

    /** If `true`, the KSP Gradle Plugin will be applied. Else, the KSP plugin can be applied manually. */
    val applyKspPlugin: Property<Boolean>

    /**
     * If `true`, the KSP generated source sets will be registered.
     *
     * Else, they can be [configured manually.](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code)
     */
    val configureGeneratedSourceSets: Property<Boolean>

    val mutableCopy: Property<Boolean>
    val copyMap: Property<Boolean>
    val hierarchyCopy: Property<Boolean>
}
