package com.johnturkson.json

import kotlin.streams.asStream
import kotlin.streams.toList


const val OPENING_OBJECT_BRACKET = "\\{"
const val CLOSING_OBJECT_BRACKET = "\\}"
const val OPENING_ARRAY_BRACKET = "\\["
const val CLOSING_ARRAY_BRACKET = "\\]"
const val OPENING_CAPTURE_GROUP_BRACKET = "("
const val CLOSING_CAPTURE_GROUP_BRACKET = ")"
const val OPENING_NON_CAPTURE_GROUP_BRACKET = "(?:"
const val CLOSING_NON_CAPTURE_GROUP_BRACKET = ")"
const val OPENING_NAMED_CAPTURE_GROUP = "?<"
const val CLOSING_NAMED_CAPTURE_GROUP = ">"
const val ARBITRARY_WHITESPACE = "\\s*"
const val DOUBLE_QUOTE = "\""
const val KEY_VALUE_SEPARATOR = "$ARBITRARY_WHITESPACE:$ARBITRARY_WHITESPACE"
const val DELIMITER = "$ARBITRARY_WHITESPACE,$ARBITRARY_WHITESPACE"
const val OPTIONAL = "?"
const val ZERO_OR_MORE = "*"
const val ONE_OR_MORE = "+"
const val OR = "|"
const val EMPTY = ""
const val INDENT_LEVEL = "    "
const val NULL = "null"
const val NEWLINE = "\n"

interface JsonElement {
    val key: String
    val type: Type
    val nullable: Boolean
    val optional: Boolean
    var parent: JsonElement?
    
    fun generateRegex(): String
    
    fun generateUnquotedRegex(value: String): String {
        return value
    }
    
    fun generateUncapturedRegex(value: String): String {
        return OPENING_NON_CAPTURE_GROUP_BRACKET + value + CLOSING_NON_CAPTURE_GROUP_BRACKET
    }
    
    fun generateCapturedRegex(value: String): String {
        val fullyQualifiedKey = fullyQualifiedKey()
        return when {
            !fullyQualifiedKey.isBlank() -> "$OPENING_CAPTURE_GROUP_BRACKET$OPENING_NAMED_CAPTURE_GROUP${fullyQualifiedKey.replace(
                ".",
                "_"
            )}$CLOSING_NAMED_CAPTURE_GROUP$value$CLOSING_CAPTURE_GROUP_BRACKET"
            else -> "$OPENING_CAPTURE_GROUP_BRACKET$value$CLOSING_CAPTURE_GROUP_BRACKET"
        }
    }
    
    
    fun generateQuotedRegex(value: String): String {
        return DOUBLE_QUOTE + value + DOUBLE_QUOTE
    }
    
    fun generateNullableRegex(value: String): String {
        return OPENING_NON_CAPTURE_GROUP_BRACKET + value + OR + NULL + CLOSING_NON_CAPTURE_GROUP_BRACKET
    }
    
    fun generateOptionalRegex(value: String): String {
        return (OPENING_NON_CAPTURE_GROUP_BRACKET + value + CLOSING_NON_CAPTURE_GROUP_BRACKET) + OPTIONAL
    }
    
    fun findGroupNumber(name: String): Int?
    
    fun findGroupNumber(name: String, previousGroup: Int): TraversalResult
    
    fun findGroupNumber(name: String, key: String, currentGroup: Int): TraversalResult
    
    fun isWithinArray(): Boolean {
        var parentCopy = this.parent
        var isInArray = false
        while (parentCopy != null) {
            if (parentCopy.type == Type.ARRAY) {
                isInArray = true
                break
            }
            parentCopy = parentCopy.parent
        }
        return isInArray
    }
    
    fun fullyQualifiedKey(): String {
        val names = mutableListOf<String>()
        var parentCopy: JsonElement? = this
        var keyCopy: String = key
        while (!keyCopy.isBlank()) {
            names.add(0, keyCopy)
            parentCopy = parentCopy?.parent
            keyCopy = parentCopy?.key ?: ""
        }
        return names.joinToString(separator = ".")
    }
}

data class TraversalResult(val group: Int, val found: Boolean)

class JsonObject(
    override val key: String,
    override val nullable: Boolean = false,
    override val optional: Boolean = false,
    override var parent: JsonElement? = null
) : JsonElement {
    override val type: Type = Type.OBJECT
    private val children: MutableSet<JsonElement> = LinkedHashSet()
    
    fun add(child: JsonElement) {
        children.add(child)
        child.parent = this
    }
    
    override fun findGroupNumber(name: String): Int? {
        if (name == fullyQualifiedKey()) {
            return 0
        }
        val result = findGroupNumber(name, 0)
        return when {
            result.found -> result.group
            else -> null
        }
    }
    
    override fun findGroupNumber(name: String, previousGroup: Int): TraversalResult {
        var currentGroup = previousGroup
        for (value in children) {
            currentGroup++
            val result = value.findGroupNumber(name, key, currentGroup)
            if (result.found) {
                return result
            }
            currentGroup = result.group
        }
        return TraversalResult(currentGroup, false)
    }
    
    override fun findGroupNumber(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> findGroupNumber(name, currentGroup)
        }
    }
    
    override fun generateRegex(): String {
        var regex = ""
        
        regex += OPENING_OBJECT_BRACKET
        regex += ARBITRARY_WHITESPACE
        
        regex += children.iterator().withIndex().asSequence().asStream()
            .map { child -> Pair(child.value, child.index + 1 == children.size) }
            .map { (child, last) ->
                when {
                    last -> Pair(child.generateRegex(), child.optional)
                    else -> Pair(child.generateRegex() + DELIMITER, child.optional)
                }
            }
            .map { (regex, optional) ->
                when {
                    optional -> OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET + OPTIONAL
                    else -> regex
                }
            }
            .toList()
            .joinToString(separator = "")
        
        regex += ARBITRARY_WHITESPACE
        regex += CLOSING_OBJECT_BRACKET
        
        if (nullable) {
            regex =
                OPENING_NON_CAPTURE_GROUP_BRACKET + OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET + OR + NULL + CLOSING_NON_CAPTURE_GROUP_BRACKET
        }
        
        if (parent != null && parent?.type != Type.ARRAY) {
            regex = when {
                isWithinArray() -> OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET
                else -> OPENING_CAPTURE_GROUP_BRACKET + regex + CLOSING_CAPTURE_GROUP_BRACKET
            }
            regex = DOUBLE_QUOTE + key + DOUBLE_QUOTE + KEY_VALUE_SEPARATOR + regex
        }
        
        return regex
    }
}

class JsonArray(
    override val key: String,
    var element: JsonElement? = null,
    override val nullable: Boolean = false,
    override val optional: Boolean = false,
    override var parent: JsonElement? = null
) : JsonElement {
    override val type: Type = Type.ARRAY
    
    init {
        require(element?.optional != true) { "Element directly inside array cannot be optional" }
        element?.parent = this
    }
    
    fun assignElement(element: JsonElement?) {
        require(element?.optional != true) { "Element directly inside array cannot be optional" }
        this.element = element
        element?.parent = this
    }
    
    override fun findGroupNumber(name: String): Int? {
        return null
    }
    
    override fun findGroupNumber(name: String, previousGroup: Int): TraversalResult {
        return TraversalResult(previousGroup, false)
    }
    
    override fun findGroupNumber(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> TraversalResult(currentGroup, false)
        }
    }
    
    override fun generateRegex(): String {
        var regex = ""
        
        regex += OPENING_ARRAY_BRACKET
        regex += ARBITRARY_WHITESPACE
        
        var arrayPattern = ""
        val pattern = element?.generateRegex() ?: ""
        if (element != null) {
            arrayPattern += OPENING_NON_CAPTURE_GROUP_BRACKET
            arrayPattern += EMPTY
            arrayPattern += OR
            arrayPattern += OPENING_NON_CAPTURE_GROUP_BRACKET
            arrayPattern += pattern
            arrayPattern += OPENING_NON_CAPTURE_GROUP_BRACKET
            arrayPattern += DELIMITER
            arrayPattern += pattern
            arrayPattern += CLOSING_NON_CAPTURE_GROUP_BRACKET
            arrayPattern += ZERO_OR_MORE
            arrayPattern += CLOSING_NON_CAPTURE_GROUP_BRACKET
            arrayPattern += CLOSING_NON_CAPTURE_GROUP_BRACKET
        }
        
        if (element?.optional == true) {
            arrayPattern =
                OPENING_NON_CAPTURE_GROUP_BRACKET + arrayPattern + CLOSING_NON_CAPTURE_GROUP_BRACKET + OPTIONAL
        }
        
        regex += arrayPattern
        
        regex += ARBITRARY_WHITESPACE
        regex += CLOSING_ARRAY_BRACKET
        
        if (nullable) {
            regex =
                OPENING_NON_CAPTURE_GROUP_BRACKET + OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET + OR + NULL + CLOSING_NON_CAPTURE_GROUP_BRACKET
        }
        
        if (parent != null && parent?.type != Type.ARRAY) {
            regex = when {
                isWithinArray() -> OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET
                else -> OPENING_CAPTURE_GROUP_BRACKET + regex + CLOSING_CAPTURE_GROUP_BRACKET
            }
            regex = DOUBLE_QUOTE + key + DOUBLE_QUOTE + KEY_VALUE_SEPARATOR + regex
        }
        
        return regex
    }
}

class JsonPrimitive(
    override val key: String,
    val value: Regex,
    override val type: Type,
    override val nullable: Boolean = false,
    override val optional: Boolean = false
) : JsonElement {
    override var parent: JsonElement? = null
    
    override fun findGroupNumber(name: String): Int? {
        return null
    }
    
    override fun findGroupNumber(name: String, previousGroup: Int): TraversalResult {
        return TraversalResult(previousGroup, false)
    }
    
    override fun findGroupNumber(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> TraversalResult(currentGroup, false)
        }
    }
    
    override fun generateRegex(): String {
        var regex = "$value"
        
        if (type == Type.STRING) {
            regex =
                DOUBLE_QUOTE + OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET + DOUBLE_QUOTE
        }
        
        if (nullable) {
            regex =
                OPENING_NON_CAPTURE_GROUP_BRACKET + OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET + OR + NULL + CLOSING_NON_CAPTURE_GROUP_BRACKET
        }
        
        regex = when {
            isWithinArray() -> OPENING_NON_CAPTURE_GROUP_BRACKET + regex + CLOSING_NON_CAPTURE_GROUP_BRACKET
            else -> OPENING_CAPTURE_GROUP_BRACKET + regex + CLOSING_CAPTURE_GROUP_BRACKET
        }
        
        if (parent?.type != Type.ARRAY) {
            regex = DOUBLE_QUOTE + key + DOUBLE_QUOTE + KEY_VALUE_SEPARATOR + regex
        }
        
        return regex
    }
}

enum class Type {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    INTEGER,
    BOOLEAN
}
