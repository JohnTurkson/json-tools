package com.johnturkson.json


fun main() {
    val testString =
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
                            "testKey2A" : [{
                                "nested1": "1",
                                "nested2": "2"
                            }],
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
    
    val schema = JsonObject("root").apply {
        add(JsonPrimitive("fs", Regex("[A-Z]+"), Type.STRING))
        add(JsonPrimitive("city", Regex(".+"), Type.STRING))
        add(JsonPrimitive("state", Regex("[A-Z]+"), Type.STRING))
        add(JsonPrimitive("country", Regex("[A-Z]+"), Type.STRING))
        add(JsonObject("times").apply {
            add(JsonObject("scheduled").apply {
                add(JsonPrimitive("time", Regex("\\d\\d?:\\d\\d"), Type.STRING))
                add(JsonPrimitive("ampm", Regex("AM|PM"), Type.STRING))
                add(JsonPrimitive("timezone", Regex("[A-Z]+"), Type.STRING))
                add(JsonObject("test").apply {
                    add(JsonPrimitive("testKey", Regex(".+"), Type.STRING))
                    add(JsonArray("testKey2A").apply {
                        assignElement(JsonObject("root").apply {
                            add(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
                            add(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
                        })
                    })
                    add(JsonPrimitive("testKey3", Regex(".+"), Type.STRING))
                })
            })
            add(JsonObject("estimated").apply {
                add(JsonPrimitive("title", Regex("Estimated|Actual"), Type.STRING))
                add(JsonPrimitive("time", Regex("\\d\\d?:\\d\\d"), Type.STRING))
                add(JsonPrimitive("ampm", Regex("AM|PM"), Type.STRING))
                add(JsonPrimitive("runway", Regex("true|false"), Type.BOOLEAN))
                add(JsonPrimitive("timezone", Regex("[A-Z]+"), Type.STRING))
            })
        })
        add(JsonPrimitive("date", Regex(".+"), Type.STRING))
    }
    
    println(schema.generateRegex())
    println(schema.generateRegex().length)
    
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
    
    
    // val test1 = JsonObject("").apply {
    //     add(JsonObject("nested").apply {
    //         add(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
    //         add(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
    //     })
    // }
    //
    // val test2 = JsonObject("root").apply {
    //     add(JsonObject("test"))
    //     add(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
    //     add(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
    // }
    //
    // val test3 = JsonObject("root").apply {
    //     add(JsonObject("a", nullable = true, optional = true))
    //     add(JsonObject("b", nullable = true, optional = false))
    //     add(JsonObject("c", nullable = false, optional = true))
    //     add(JsonObject("d", nullable = false, optional = false))
    //     add(JsonPrimitive("nested1", Regex(".+"), Type.STRING))
    //     add(JsonPrimitive("nested2", Regex(".+"), Type.STRING))
    //     add(JsonPrimitive("blank", Regex(".+"), Type.STRING))
    // }
    //
    // val test4 = JsonArray("").apply {
    //     assignElement(JsonObject("k", nullable = true, optional = false).apply {
    //         add(JsonPrimitive("k1", Regex("\\d+"), Type.INTEGER))
    //     })
    // }
    
    // val test5 = 
    //     JsonObject("").apply {
    //     add(
    //         JsonArray("array", nullable = false).apply {
    //         assignElement(JsonArray("array2", nullable = true).apply {
    //             // assignElement(JsonArray("array3", nullable = false).apply {
    //                 assignElement(JsonPrimitive("k1", Regex("\\d+"), Type.INTEGER))
    //             // })
    //         })
    //     }
    //     )
    // }
    
    // println(test1.generateRegex())
    // println(test2.generateRegex())
    // println(test3.generateRegex())
    // println(test4.generateRegex())
    // println(test5.generateRegex())
    // println()
    // println(test1.findGroupNumber("nested"))
    // println(test1.findGroupNumber("nested.nested1"))
    // println(test1.findGroupNumber("nested.nested2"))
    // println(test1.findGroupNumber("doesnotexist"))
    // println(test1.findGroupNumber("nested.doesnotexist"))
    // println()
    // println(test2.findGroupNumber("root"))
    // println(test2.findGroupNumber("root.test"))
    // println(test2.findGroupNumber("root.nested1"))
    // println(test2.findGroupNumber("root.nested2"))
    // println(test2.findGroupNumber("doesnotexist"))
    // println(test2.findGroupNumber("root.doesnotexist"))
    // println()
    // println(test3.findGroupNumber("root"))
    // println(test3.findGroupNumber("root.nested1"))
    // println(test3.findGroupNumber("root..nested1"))
    // println()
    // println(test5.findGroupNumber(""))
    // println(test5.findGroupNumber("array"))
    // println(test5.findGroupNumber("array.array2"))
    // println(test5.findGroupNumber("root.array"))
}
