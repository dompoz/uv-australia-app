package com.uvaustralia.app.data

import com.uvaustralia.app.domain.UvReading
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object ArpansaXmlParser {
    private const val XML_URL = "https://uvdata.arpansa.gov.au/xml/uvvalues.xml"

    fun parseXml(xml: String): List<UvReading> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(xml)))
        doc.documentElement.normalize()

        val readings = mutableListOf<UvReading>()
        val locations = doc.getElementsByTagName("location")
        for (i in 0 until locations.length) {
            val node = locations.item(i)
            val children = node.childNodes
            var name = ""; var index = 0.0; var time = ""; var date = ""; var status = ""
            for (j in 0 until children.length) {
                val child = children.item(j)
                when (child.nodeName) {
                    "name"   -> name   = child.textContent.trim()
                    "index"  -> index  = child.textContent.trim().toDoubleOrNull() ?: 0.0
                    "time"   -> time   = child.textContent.trim()
                    "date"   -> date   = child.textContent.trim()
                    "status" -> status = child.textContent.trim()
                }
            }
            if (name.isNotEmpty()) {
                readings += UvReading(name, index, time, date, status)
            }
        }
        return readings
    }

    fun url() = XML_URL
}
