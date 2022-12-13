@file:JsModule("electron")

package ext.electron

import model.TerminalSession
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
    fun send(eventName: String, id:Int, param: String)
    fun send(eventName: String, param: String)
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
external interface IpcRenderer {
    fun send(eventName: String, id:Int, param: String)
    fun send(eventName: String, param: String)
    fun on(eventName: String, listener: (Event, Int, String) -> Unit)
    fun on(eventName: String, listener: (Event, String) -> Unit)
    fun <T> invoke(eventName: String, params: dynamic): T


}

// ipcMain
external val ipcMain: IpcMain
external interface IpcMain {
    fun on(eventName: String, listener: (Event, Int, String) -> Unit)
    fun on(eventName: String, listener: (Event, String) -> Unit)
    fun <T> handle(eventName: String, listener: (Event, dynamic) -> T)
}

