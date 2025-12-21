package de.lschnartzke.fscramble.runner

import de.lschnartzke.fscramble.config.RatioConfig
import de.lschnartzke.fscramble.config.RunConfig

abstract class AbstractRunner {
    companion object {
        fun fromRunConfig(config: RunConfig): AbstractRunner {
            return when (config) {
                is RunConfig.Scramble -> ScrambleRunner(config)
                is RunConfig.Create -> CreateRunner(config)
            }
        }
    }

    var ratios: RatioConfig? = null

    abstract fun run()
}