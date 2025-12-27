package de.lschnartzke.fscramble.config

import java.io.ObjectInputFilter

open class ConfigException(val file: String, message: String) : Exception("$file: $message")

class UnexpectedTopLevelNodeCount(file: String, val expected: Int, val got: Int) :
    ConfigException(file, "Expected $expected top-level nodes, got $got instead.")

class UnexpectedNodeName(file: String, val name: String, val expected: String? = null)
    : ConfigException(file,
    if (expected != null) "Expected '$expected', got node '$name' instead, configuration is invalid"
            else "Node name '$name' not expected, configuration is invalid")

class UnexpectedPropertyCount(file: String, val node: String, val expected: Int, val got: Int)
    : ConfigException(file, "Unexpected number of properties on node '$node': expected '$expected', got '$got' instead")

class UnexpectedPropertyName(file: String, val propName: String, val nodeName: String)
    : ConfigException(file, "Unexpected property name '$propName' on node '$nodeName'")

class UnexpectedNumberOfArguments(file: String, val nodeName: String, val expected: Int, val got: Int)
    : ConfigException(file, "Unexpected Number of arguments on node '$nodeName', expected '$expected' arguments, got '$got' instead")

class UnexpectedChildCount(file: String, val nodeName: String, val expected: Int, val got: Int)
    : ConfigException(file, "Unexpected number of children on node '$nodeName': expected '$expected', got '$got' instead.")

class IllegalArgumentType(file: String, val nodeName: String, val expected: String, val got: String)
    : ConfigException(file,  "Illegal argument type on node '$nodeName': Expected type '$expected', got '$got' instead.")