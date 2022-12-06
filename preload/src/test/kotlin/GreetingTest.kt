@file:JsExport

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.get

fun preload3() {
    fun replaceText(selector: String, text: String) {
        document.getElementById(selector)?.innerHTML = text
    }
    arrayOf("chrome", "node", "electron").forEach {
        //replaceText(it, process.versions)
        kotlin.js.console.dir(process.versions)
    }
}
