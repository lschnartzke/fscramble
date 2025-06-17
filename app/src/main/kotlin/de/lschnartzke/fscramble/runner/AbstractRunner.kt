package de.lschnartzke.fscramble.runner

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

    abstract fun run()
}