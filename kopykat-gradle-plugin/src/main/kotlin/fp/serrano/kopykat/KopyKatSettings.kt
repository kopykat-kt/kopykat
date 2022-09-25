package fp.serrano.kopykat

import org.gradle.api.provider.Property

interface KopyKatSettings {

    /**
     * If `true`, the KSP generated source sets will be registered as part of the `main` source set.
     * Otherwise, they can be [configured manually.](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code)
     */
    val configureGeneratedSourceSets: Property<Boolean>

    val mutableCopy: Property<Boolean>
    val copyMap: Property<Boolean>
    val hierarchyCopy: Property<Boolean>
}
