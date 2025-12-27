package de.lschnartzke.fscramble.config

import com.github.ajalt.clikt.completion.CompletionCandidates
import dev.kdl.KdlDocument
import dev.kdl.KdlNode
import dev.kdl.KdlNumber
import dev.kdl.parse.KdlHybridParser
import dev.kdl.parse.KdlParseException
import dev.kdl.parse.KdlParser
import dev.kdl.parse.Reporter
import java.io.File
import java.math.BigInteger
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

private fun parseScrambleCount(file: String, node: KdlNode): Int {
    assert (node.name == "scramble-count")

    if (node.properties.propertyNames().isNotEmpty()) {
        throw UnexpectedPropertyCount(file, node.name, 0, node.properties.propertyNames().size)
    }

    if (node.children.isNotEmpty()) {
        throw UnexpectedChildCount(file, node.name, 0, node.children.size)
    }

    if (node.arguments.size != 1) {
        throw UnexpectedNumberOfArguments(file, node.name, 1, node.arguments.size)
    }

    val scrambleArg = node.arguments.first()
    if (!scrambleArg.isNumber) {
        throw IllegalArgumentType(file, node.name, "number", scrambleArg.type() ?: "<undefined>")
    }

    // Can't be bothered to deal with KDL4Js' API to cast values to types
    return scrambleArg.toString().toInt()
}

class Configuration(
    val runConfigs: List<RunConfiguration>
) {
    companion object {
        private fun fromKdlDocument(file: String, doc: KdlDocument): Configuration {
            if (doc.nodes.size != 1) {
                throw UnexpectedTopLevelNodeCount(file, 1, doc.nodes.size)
            }

            val node = doc.nodes.first()
            if (node.name != "runs") {
                throw UnexpectedNodeName(file, node.name, "runs")
            }

            // TODO: Loop over child nodes, looking for 'run-config' with name property.
            // Fail on missing property or wrong node name
            val runConfigs: MutableList<RunConfiguration> = mutableListOf()
            for (node in doc.nodes) {
                when (node.name) {
                    "run-config" -> runConfigs.add(RunConfiguration.fromKdlNode(file, node))
                }
            }

            return Configuration(runConfigs)
        }
    }
}

class RunConfiguration(
    val name: String,
    val scrambleCount: Int,
    stages: List<RunStage>,
) {
    companion object {

        fun fromKdlNode(file: String, node: KdlNode): RunConfiguration {
            assert (node.name == "run-config")

            if (node.properties.propertyNames().size != 1) {
                throw UnexpectedPropertyCount(file, node.name, 1, node.properties.propertyNames().size)
            }

            val propName = node.properties.propertyNames().first()
            if (propName != "name") {
                throw UnexpectedPropertyName(file, propName, node.name)
            }

            val configName = node.properties.getValue<String>(propName).get().toString()
            val stages: MutableList<RunStage> = mutableListOf()
            var scrambleCount: Int = 0

            var currentOrder = 0
            for (childNode in node.children) {
                when (childNode.name) {
                    "scramble-count" -> scrambleCount = parseScrambleCount(file, childNode)
                    "stage" ->  stages.add(RunStage.fromKdlNode(file, childNode, currentOrder++))
                }
            }

            return RunConfiguration(configName, scrambleCount, stages)
        }
    }
}

class RunStage(
    // properties
    val order: Int,
    val create: Boolean,
    val scramble: Boolean,

    // child nodes
    val inputDirectory: File,
    val outputDirectory: File,
    val archiveData: File,
    val files: List<FileConfig>
    ) {
    companion object {
        fun fromKdlNode(file: String, node: KdlNode, defaultOrder: Int): RunStage {
            assert (node.name == "stage")

            // optional properties
            var gotOrder = false
            var gotCreate = false
            var gotScramble = false

            var order: Int? = null
            var create = true
            var scramble = true
            for (propName in node.properties.propertyNames()) {
                when (propName) {
                    "order" -> {
                        order = node.properties.getValue<KdlNumber.Integer>(propName).getOrNull()?.value()?.asInt()
                        gotOrder = true
                    }
                    "create" -> {
                        create = node.properties.getValue<Boolean>(propName).getOrNull()?.value() ?: true
                        gotCreate = true
                    }
                    "scramble" -> {
                        scramble = node.properties.getValue<Boolean>(propName).getOrNull()?.value() ?: true
                        gotScramble = true
                    }
                    else -> throw UnexpectedPropertyName(file, propName, node.name)
                }
            }

            var inputDirectory: String? = null
            var outputDirectory: String? = null
            var archiveData: String? = null

            for (childNode in node.children) {
                when (childNode.name) {
                    "input-directory" -> {

                    }

                    "output-directory" -> {

                    }

                    "archive-data" -> {

                    }

                    "files" -> {

                    }
                }
            }
        }
    }
}

class FileConfig(
    // node name
    val extension: String,
    // mutually exclusive properties
    val ratio: BigInteger?,
    val count: Integer,
)



fun loadConfigurationFromFile(file: File): Configuration {
    val configDocument: KdlDocument
    try {
        val parser = KdlHybridParser()
        configDocument = parser.parse(file.toPath())
    } catch (kdlEx: KdlParseException) {
        val errString = if (System.console() != null) {
            Reporter.getReportWithAnsiCodes(kdlEx)
        } else {
            Reporter.getReport(kdlEx)
        }

        println("Failed to parse config")
        println(errString)
        exitProcess(1)
    }

}
