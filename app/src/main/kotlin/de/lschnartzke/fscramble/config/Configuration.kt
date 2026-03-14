package de.lschnartzke.fscramble.config

import dev.kdl.KdlDocument
import dev.kdl.KdlNode
import dev.kdl.KdlNumber
import dev.kdl.parse.KdlHybridParser
import dev.kdl.parse.KdlParseException
import dev.kdl.parse.KdlParser
import dev.kdl.parse.Reporter
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

private fun convolutedParseIntFromKdlValueString(string: String?): Int {
    string ?: return 0
    val intString = Regex("([0-9]+)").find(string)!!.value
    return intString.toInt()
}

private fun convolutedParseBigDecimalFromKdlValueString(string: String?) : BigDecimal {
    string ?: return BigDecimal.ZERO
    val decimalString = Regex("([0-9]+\\.?[0-9]*)").find(string)!!.value
    return BigDecimal.valueOf(decimalString.toDouble())
}

private fun parseScrambleCount(file: String, node: KdlNode): Int {
    assert(node.name == "scramble-count")

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

    // this does not feel right, but unfortunately works.
    val scrambleValue = scrambleArg.value() as BigInteger
    return scrambleValue.toInt()
}

private fun parseSingleArgumentNode(file: String, node: KdlNode): String {
    if (node.properties.propertyNames().isNotEmpty()) {
        throw UnexpectedPropertyCount(file, node.name, 0, node.properties.propertyNames().size)
    }

    if (node.children.isNotEmpty()) {
        throw UnexpectedChildCount(file, node.name, 0, node.children.size)
    }

    if (node.arguments.size != 1) {
        throw UnexpectedNumberOfArguments(file, node.name, 1, node.arguments.size)
    }

    // TODO: Do this properly using the value-method
    val argument = node.arguments.first().toString()
    return argument
}

class Configuration(
    val runConfigs: List<RunConfiguration>
) {
    companion object {
        fun fromKdlDocument(file: String, doc: KdlDocument): Configuration {
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
            for (childNode in node.children) {
                when (childNode.name) {
                    "run-config" -> runConfigs.add(RunConfiguration.fromKdlNode(file, childNode))
                    else -> throw UnexpectedNodeName(file, childNode.name, "run-config")
                }
            }

            return Configuration(runConfigs)
        }
    }
}

class RunConfiguration(
    val name: String,
    val scrambleCount: Int,
    val stages: List<RunStage>,
) {
    companion object {

        fun fromKdlNode(file: String, node: KdlNode): RunConfiguration {
            assert(node.name == "run-config")

            if (node.properties.propertyNames().size != 1) {
                throw UnexpectedPropertyCount(file, node.name, 1, node.properties.propertyNames().size)
            }

            val propName = node.properties.propertyNames().first()
            if (propName != "name") {
                throw UnexpectedPropertyName(file, propName, node.name)
            }

            val configName = node.properties.getValue<String>(propName).get().value()
            val stages: MutableList<RunStage> = mutableListOf()
            var scrambleCount: Int = 0

            var currentOrder = 0
            for (childNode in node.children) {
                when (childNode.name) {
                    "scramble-count" -> scrambleCount = parseScrambleCount(file, childNode)
                    "stage" -> stages.add(RunStage.fromKdlNode(file, childNode, currentOrder++))
                }
            }

            // sort stages based on custom order key
            // TODO: Deal with duplicate order keys or simply call it undefined behavior?
            stages.sortBy { it.order }

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
    val inputDirectory: Path,
    val outputDirectory: Path,
    val archiveData: Path,
    val files: List<FileConfig>
) {
    companion object {
        fun fromKdlNode(file: String, node: KdlNode, defaultOrder: Int): RunStage {
            assert(node.name == "stage")

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
                        order = convolutedParseIntFromKdlValueString(node.properties.getValue<KdlNumber.Integer>(propName).getOrNull()?.toString())
                    }

                    "create" -> {
                        create = node.properties.getValue<Boolean>(propName).getOrNull()?.value() ?: true
                    }

                    "scramble" -> {
                        scramble = node.properties.getValue<Boolean>(propName).getOrNull()?.value() ?: true
                    }

                    else -> throw UnexpectedPropertyName(file, propName, node.name)
                }
            }

            var inputDirectory: String? = null
            var outputDirectory: String? = null
            var archiveData: String? = null
            val fileConfigs: MutableList<FileConfig> = mutableListOf()

            for (childNode in node.children) {
                when (childNode.name) {
                    "input-directory" -> inputDirectory = parseSingleArgumentNode(file, childNode)
                    "output-directory" -> outputDirectory = parseSingleArgumentNode(file, childNode)
                    "archive-data" -> archiveData = parseSingleArgumentNode(file, childNode)
                    "files" -> childNode.children.forEach { fileNode -> fileConfigs.add(FileConfig.fromKdlNode(file, fileNode)) }
                }
            }

            if (inputDirectory == null) {
                throw ArgumentRequired(file, "input-directory")
            }

            if (outputDirectory == null) {
                throw ArgumentRequired(file, "output-directory")
            }

            if (archiveData == null) {
                throw ArgumentRequired(file, "archive-data")
            }

            return RunStage(
                order ?: defaultOrder,
                create,
                scramble,
                Path.of(inputDirectory),
                Path.of(outputDirectory),
                Path.of(archiveData),
                fileConfigs
            )
        }
    }
}

class FileConfig(
    // node name
    val extension: String,
    // mutually exclusive properties
    val ratio: BigDecimal?,
    val count: Int?,
) {
    companion object {
        fun fromKdlNode(file: String, node: KdlNode): FileConfig {
            val extension = node.name

            var gotRatio = false
            var gotCount = false
            var count: Int? = null
            var ratio:  BigDecimal? = null

            for (propName in node.properties.propertyNames()) {
                when (propName) {
                    "count" -> {
                        val valueOpt = node.properties.getValue<KdlNumber.Integer>(propName)
                        if (valueOpt.isEmpty) {
                            throw PropertyValueExpected(file, node.name, propName)
                        }

                        val value = valueOpt.get()

                        if (!value.isNumber) {
                            throw IllegalPropertyValueType(file, node.name, propName, "number", value.type() ?: "<undefined>")
                        }

                        count = convolutedParseIntFromKdlValueString(value.toString())
                        gotCount = true
                    }

                    "ratio" -> {
                        val valueOpt = node.properties.getValue<KdlNumber.Decimal>(propName)
                        if (valueOpt.isEmpty) {
                            throw PropertyValueExpected(file, node.name, propName)
                        }

                        val value = valueOpt.get()

                        if (!value.isNumber) {
                            throw IllegalPropertyValueType(file, node.name, propName, "number", value.type() ?: "<undefined>")
                        }

                        ratio = convolutedParseBigDecimalFromKdlValueString(value.toString())
                        gotRatio = true
                    }

                    else -> {
                        throw UnexpectedPropertyName(file, propName, node.name)
                    }
                }
            }

            if (gotCount && gotRatio) {
                throw MutuallyExclusiveProperties(file, node.name, "count", "ratio")
            }

            return FileConfig(extension, ratio, count)
        }
    }
}


fun loadConfigurationFromFile(file: File): Configuration {
    val configDocument: KdlDocument
    try {
        val parser = KdlParser.v1()
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

    return Configuration.fromKdlDocument(file.toString(), configDocument)
}
