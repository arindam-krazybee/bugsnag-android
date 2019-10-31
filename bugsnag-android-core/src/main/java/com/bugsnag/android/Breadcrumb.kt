package com.bugsnag.android

import java.io.IOException
import java.io.StringWriter
import java.util.Date
import java.util.HashMap

class Breadcrumb internal constructor(
    val message: String,
    val type: BreadcrumbType,
    metadata: MutableMap<String, Any>,
    captureDate: Date = Date()
) : JsonStream.Streamable {

    val timestamp: String = DateUtils.toIso8601(captureDate)
    val metadata: MutableMap<String, Any> = HashMap(metadata)

    internal constructor(message: String) : this(
        "manual",
        BreadcrumbType.MANUAL,
        mutableMapOf(Pair("message", message)),
        Date()
    )

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("timestamp").value(timestamp)
        writer.name("name").value(message)
        writer.name("type").value(type.toString())
        writer.name("metaData")
        writer.beginObject()

        // sort metadata alphabetically
        metadata.entries.sortedBy { it.key }
            .forEach { writer.name(it.key).value(it.value) }

        writer.endObject()
        writer.endObject()
    }
}