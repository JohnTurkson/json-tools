package com.johnturkson.json

fun main() {
    val test1 = JsonObject().apply {
        addProperty("nested", JsonObject("nested").apply {
            addProperty("nested1", JsonPrimitive("nested1", Regex(".+"), Type.STRING))
            addProperty("nested2", JsonPrimitive("nested2", Regex(".+"), Type.STRING))
        })
    }

    val test2 = JsonObject("root").apply {
        addProperty("nested1", JsonPrimitive("nested1", Regex(".+"), Type.STRING))
        addProperty("nested2", JsonPrimitive("nested2", Regex(".+"), Type.STRING))
    }
    
    println(test1.findGroup("nested"))
    println(test1.findGroup("nested.nested1"))
    println(test1.findGroup("nested.nested2"))
    println(test1.findGroup("doesnotexist"))
    println(test1.findGroup("nested.doesnotexist"))

    println(test2.findGroup("root"))
    println(test2.findGroup("root.nested1"))
    println(test2.findGroup("root.nested2"))
    println(test2.findGroup("doesnotexist"))
    println(test2.findGroup("root.doesnotexist"))
}
