package fp.serrano.kopykat

import org.gradle.api.provider.Property

interface KopyKatSettings {
    val applyKspPlugin: Property<Boolean>

    val mutableCopy: Property<Boolean>
    val copyMap: Property<Boolean>
    val hierarchyCopy: Property<Boolean>
}
