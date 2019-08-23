package com.johnturkson.json

fun main() {
    val test =
        """{
                "fs": "CLT",
                "city": "Charlotte",
                "state": "NC",
                "country": "US",
                "times": {
                    "scheduled": {
                        "time": "4:15",
                        "ampm": "PM",
                        "timezone": "EDT",
                        "test": {
                            "testKey": "testK1",
                            "testKey2A": [
                                {
                                    "nested1": "value1",
                                    "nested2": "value2"
                                }
                            ],
                            "testKey3": "testK3"
                        }
                    },
                    "estimated": {
                        "title": "Estimated",
                        "time": "4:15",
                        "ampm": "PM",
                        "runway": false,
                        "timezone": "EDT"
                    }
                },
                "date": "2019-08-20T16:15:00.000"
            }""".trimIndent()
    
    val schema = JsonObject().apply {
        addProperty(JsonPrimitive("fs", Regex("[A-Z]+"), Type.STRING))
        addProperty(JsonPrimitive("city", Regex(".+"), Type.STRING))
        addProperty(JsonPrimitive("state", Regex("[A-Z]+"), Type.STRING))
        addProperty(JsonPrimitive("country", Regex("[A-Z]+"), Type.STRING))
        addProperty(JsonObject("times").apply {
            addProperty(JsonObject("scheduled").apply {
                addProperty(JsonPrimitive("time", Regex("\\d\\d?:\\d\\d"), Type.STRING))
                addProperty(JsonPrimitive("ampm", Regex("AM|PM"), Type.STRING))
                addProperty(JsonPrimitive("timezone", Regex("[A-Z]+"), Type.STRING))
                addProperty(JsonObject("test").apply {
                    addProperty(JsonPrimitive("testKey", Regex(".+"), Type.STRING))
                    addProperty(JsonArray("testKey2A").apply {
                        assignElement(JsonObject().apply {
                            addProperty(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
                            addProperty(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
                        })
                    })
                    addProperty(JsonPrimitive("testKey3", Regex(".+"), Type.STRING))
                })
            })
            addProperty(JsonObject("estimated").apply {
                addProperty(JsonPrimitive("title", Regex("Estimated|Actual"), Type.STRING))
                addProperty(JsonPrimitive("time", Regex("\\d\\d?:\\d\\d"), Type.STRING))
                addProperty(JsonPrimitive("ampm", Regex("AM|PM"), Type.STRING))
                addProperty(JsonPrimitive("runway", Regex("true|false"), Type.BOOLEAN))
                addProperty(JsonPrimitive("timezone", Regex("[A-Z]+"), Type.STRING))
            })
        })
        addProperty(JsonPrimitive("date", Regex(".+"), Type.STRING))
    }
    
    println(schema.generateRegex())
    // println(schema.fullyQualifiedKey())
    // println(schema.findGroup("fs"))
    // println(schema.findGroup("city"))
    // println(schema.findGroup("times"))
    // println(schema.findGroup("times.scheduled.test"))
    // println(schema.findGroup("times.scheduled.test.testKey"))
    // println(schema.findGroup("times.scheduled.test.testKey2A"))
    // println(schema.findGroup("times.scheduled.test.testKey3"))
    // println(schema.findGroup("times.estimated"))
    // println(schema.findGroup("times.estimated.title"))
    // println(schema.findGroup("times.estimated.time"))
    // println(schema.findGroup("date"))
    
    //
    // val test3 = JsonObject().apply {
    //     addProperty("scheduled", JsonObject().apply {
    //         addProperty("time", JsonPrimitive(Type.STRING, Regex("\\d\\d?:\\d\\d")))
    //         addProperty("ampm", JsonPrimitive(Type.STRING, Regex("AM|PM")))
    //         addProperty("timezone", JsonPrimitive(Type.STRING, Regex("[A-Z]+")))
    //         addProperty("test", JsonObject().apply {
    //             addProperty("testKey", JsonPrimitive(Type.STRING, Regex("testK1")))
    //             // addProperty(
    //             //     "testKey2A", JsonArray().apply {
    //             //         assignElement(JsonObject().apply {
    //             //             addProperty("nested1", JsonPrimitive(Type.STRING, Regex(".+")))
    //             //             addProperty("nested2", JsonPrimitive(Type.STRING, Regex(".+")))
    //             //         })
    //             //     })
    //             addProperty("testKey3", JsonPrimitive(Type.STRING, Regex("testK3")))
    //         })
    //     })
    //     addProperty("estimated", JsonObject().apply {
    //         addProperty("title", JsonPrimitive(Type.STRING, Regex("Estimated|Actual")))
    //         addProperty("time", JsonPrimitive(Type.STRING, Regex("\\d\\d?:\\d\\d")))
    //         addProperty("ampm", JsonPrimitive(Type.STRING, Regex("AM|PM")))
    //         addProperty("runway", JsonPrimitive(Type.BOOLEAN, Regex("true|false")))
    //         addProperty("timezone", JsonPrimitive(Type.STRING, Regex("[A-Z]+")))
    //     })
    // }
    //
    // val test4 = JsonObject().apply {
    //     addProperty("t1", JsonPrimitive("t1", Regex("asdf"), Type.STRING, nullable = true, optional = true))
    //     addProperty("t2", JsonPrimitive("t2", Regex("asd"), Type.STRING))
    //     addProperty("t3", JsonObject().apply {
    //         addProperty("t4", JsonPrimitive(Type.STRING, Regex("t4")))
    //     })
    //     addProperty("t5", JsonObject().apply {
    //         addProperty("t6", JsonObject().apply {
    //             addProperty("t7", JsonPrimitive(Type.STRING, Regex("t7")))
    //         })
    //     })
    // }
    
    
    val test1 = JsonObject().apply {
        addProperty(JsonObject("nested").apply {
            addProperty(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
            addProperty(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
        })
    }
    
    val test2 = JsonObject("root").apply {
        addProperty(JsonObject("test"))
        addProperty(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
        addProperty(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
    }
    
    println(test1.findGroup("nested"))
    println(test1.findGroup("nested.nested1"))
    println(test1.findGroup("nested.nested2"))
    println(test1.findGroup("doesnotexist"))
    println(test1.findGroup("nested.doesnotexist"))

    println(test2.findGroup("root"))
    println(test2.findGroup("root.test"))
    println(test2.findGroup("root.nested1"))
    println(test2.findGroup("root.nested2"))
    println(test2.findGroup("doesnotexist"))
    println(test2.findGroup("root.doesnotexist"))
}
