package com.johnturkson.json

const val OPENING_OBJECT_BRACKET = "\\{"
const val CLOSING_OBJECT_BRACKET = "\\}"
const val OPENING_ARRAY_BRACKET = "\\["
const val CLOSING_ARRAY_BRACKET = "\\]"
const val OPENING_CAPTURE_GROUP_BRACKET = "("
const val CLOSING_CAPTURE_GROUP_BRACKET = ")"
const val OPENING_NON_CAPTURE_GROUP_BRACKET = "(?:"
const val CLOSING_NON_CAPTURE_GROUP_BRACKET = ")"
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
    val key: String?
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
        return OPENING_CAPTURE_GROUP_BRACKET + value + CLOSING_CAPTURE_GROUP_BRACKET
    }

    fun generateQuotedRegex(value: String): String {
        return DOUBLE_QUOTE + OPENING_NON_CAPTURE_GROUP_BRACKET + value + CLOSING_NON_CAPTURE_GROUP_BRACKET + DOUBLE_QUOTE
    }

    fun generateNullableRegex(value: String): String {
        return OPENING_NON_CAPTURE_GROUP_BRACKET + value + OR + NULL + CLOSING_NON_CAPTURE_GROUP_BRACKET
    }

    fun generateOptionalRegex(value: String): String {
        return (OPENING_NON_CAPTURE_GROUP_BRACKET + value + CLOSING_NON_CAPTURE_GROUP_BRACKET) + OPTIONAL
    }

    fun findGroup(name: String): Int?

    fun findGroup(name: String, previousGroup: Int): TraversalResult

    fun findGroup(name: String, key: String, currentGroup: Int): TraversalResult

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

    fun fullyQualifiedKey(): String? {
        val names = mutableListOf<String>()
        var parentCopy: JsonElement? = this
        var keyCopy: String? = key
        while (keyCopy != null && !keyCopy.isBlank()) {
            names.add(0, keyCopy)
            parentCopy = parentCopy?.parent
            keyCopy = parentCopy?.key
        }
        return names.joinToString(separator = ".")
    }
}

data class TraversalResult(val group: Int, val found: Boolean)

data class JsonObject(
    override val key: String? = null,
    val properties: MutableMap<String, JsonElement> = LinkedHashMap(),
    override val nullable: Boolean = false,
    override val optional: Boolean = false,
    override var parent: JsonElement? = null
) : JsonElement {
    override val type: Type = Type.OBJECT

    // TODO finding group indices: traverse through entire tree while keeping track of current position, and if the fully qualified name matches the key, then return the value

    fun addProperty(key: String, value: JsonElement) {
        properties[key] = value
        value.parent = this
    }

    override fun findGroup(name: String): Int? {
        if (name == fullyQualifiedKey()) {
            return 1
        }
        val result = findGroup(name, 1)
        return when {
            result.found -> result.group
            else -> null
        }
    }

    override fun findGroup(name: String, previousGroup: Int): TraversalResult {
        var currentGroup = previousGroup
        for ((key, value) in properties) {
            currentGroup++
            val result = value.findGroup(name, key, currentGroup)
            if (result.found) {
                return result
            }
            currentGroup = result.group
        }
        return TraversalResult(currentGroup, false)
    }

    override fun findGroup(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> findGroup(name, currentGroup)
        }
    }

    override fun generateRegex(): String {
        val builder = StringBuilder()
        when {
            isWithinArray() -> builder.append(OPENING_NON_CAPTURE_GROUP_BRACKET)
            else -> builder.append(OPENING_CAPTURE_GROUP_BRACKET)
        }

        builder.append(OPENING_OBJECT_BRACKET)
        builder.append(ARBITRARY_WHITESPACE)
        builder.append(
            properties.entries.joinToString(
                separator = DELIMITER
            ) {
                "$DOUBLE_QUOTE${it.key}$DOUBLE_QUOTE$KEY_VALUE_SEPARATOR${it.value.generateRegex()}"
            }
        )
        builder.append(ARBITRARY_WHITESPACE)
        builder.append(CLOSING_OBJECT_BRACKET)

        when {
            isWithinArray() -> builder.append(CLOSING_NON_CAPTURE_GROUP_BRACKET)
            else -> builder.append(CLOSING_CAPTURE_GROUP_BRACKET)
        }

        var regex = builder.toString()

        if (optional) {
            regex = generateOptionalRegex(regex)
        }

        if (nullable) {
            regex = generateNullableRegex(regex)
        }

        return regex
    }
}

data class JsonArray(
    override val key: String? = null,
    var element: JsonElement? = null,
    override val nullable: Boolean = false,
    override val optional: Boolean = false,
    override var parent: JsonElement? = null
) : JsonElement {
    override val type: Type = Type.ARRAY

    init {
        if (element?.optional == true) {
            throw IllegalArgumentException("Element directly inside array cannot be optional")
        }
        element?.parent = this
    }

    fun assignElement(element: JsonElement?) {
        if (element?.optional == true) {
            throw IllegalArgumentException("Element directly inside array cannot be optional")
        }
        this.element = element
        element?.parent = this
    }

    override fun findGroup(name: String): Int? {
        return null
    }

    override fun findGroup(name: String, previousGroup: Int): TraversalResult {
        return TraversalResult(previousGroup, false)
    }

    override fun findGroup(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> TraversalResult(currentGroup, false)
        }
    }

    override fun generateRegex(): String {
        val builder = StringBuilder()
        builder.append(OPENING_ARRAY_BRACKET)
        builder.append(ARBITRARY_WHITESPACE)
        builder.append(OPENING_CAPTURE_GROUP_BRACKET)
        builder.append(ARBITRARY_WHITESPACE)

        val regex = element?.generateRegex() ?: ""
        if (element != null) {
            builder.append(OPENING_NON_CAPTURE_GROUP_BRACKET)
            builder.append(EMPTY)
            builder.append(OR)
            builder.append(OPENING_NON_CAPTURE_GROUP_BRACKET)
            builder.append(regex)
            builder.append(OPENING_NON_CAPTURE_GROUP_BRACKET)
            builder.append(ARBITRARY_WHITESPACE + DELIMITER + ARBITRARY_WHITESPACE)
            builder.append(regex)
            builder.append(CLOSING_NON_CAPTURE_GROUP_BRACKET)
            builder.append(ZERO_OR_MORE)
            builder.append(CLOSING_NON_CAPTURE_GROUP_BRACKET)
            builder.append(CLOSING_NON_CAPTURE_GROUP_BRACKET)
        } else {
            builder.append(EMPTY)
        }

        builder.append(ARBITRARY_WHITESPACE)
        builder.append(CLOSING_CAPTURE_GROUP_BRACKET)
        builder.append(ARBITRARY_WHITESPACE)
        builder.append(CLOSING_ARRAY_BRACKET)
        return builder.toString()
    }
}

data class JsonPrimitive(
    override val key: String? = null,
    val value: Regex,
    override val type: Type,
    override val nullable: Boolean = false,
    override val optional: Boolean = false,
    override var parent: JsonElement? = null
) : JsonElement {

    override fun findGroup(name: String): Int? {
        return null
    }

    override fun findGroup(name: String, previousGroup: Int): TraversalResult {
        return TraversalResult(previousGroup, false)
    }

    override fun findGroup(name: String, key: String, currentGroup: Int): TraversalResult {
        return when (name) {
            fullyQualifiedKey() -> TraversalResult(currentGroup, true)
            else -> TraversalResult(currentGroup, false)
        }
    }

    override fun generateRegex(): String {
        var regex = "$value"
        if (type == Type.STRING) {
            regex = generateQuotedRegex(regex)
        }
        if (nullable) {
            regex = generateNullableRegex(regex)
        }

        if (isWithinArray()) {
            regex = generateUncapturedRegex(regex)
        } else {
            regex = generateCapturedRegex(regex)
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
