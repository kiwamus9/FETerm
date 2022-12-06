@file:JsModule("electron")
package ext.electron

import org.w3c.dom.events.Event
import kotlin.js.Promise

external val app: App
external class BrowserWindow(init: dynamic) {
    val webContents: WebContents
    fun loadFile(filePath: String): Unit
    companion object {
        fun getAllWindows():Array<BrowserWindow>
    }
}

external interface App {
    fun on(eventName: String, listener:(Event) -> Unit)
    fun whenReady(): Promise<Any>
    fun quit()
}

external interface WebContents {
    fun openDevTools(options: dynamic = definedExternally)
}

