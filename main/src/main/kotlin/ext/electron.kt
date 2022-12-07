@file:JsModule("electron")

package ext.electron

import org.w3c.dom.events.Event
import kotlin.js.Promise

// BrowserWindow
external class BrowserWindow(init: dynamic) {
    val webContents: WebContents
    fun loadFile(filePath: String): Unit

    companion object {
        fun getAllWindows(): Array<BrowserWindow>
    }
}

external interface WebContents {
    fun openDevTools(options: dynamic = definedExternally)
}

// app
external val app: App

external interface App {
    fun on(eventName: String, listener: (Event) -> Unit)
    fun whenReady(): Promise<Any>
    fun quit()
}

// contextBridge
external val contextBridge: ContextBridge
external interface ContextBridge {
    fun exposeInMainWorld(name: String, obj: Any)
}

// ipcRenderer
external val ipcRenderer: IpcRenderer
external interface IpcRenderer {}


