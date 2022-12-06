import ext.electron.App
import ext.electron.BrowserWindow
import path.path

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}

lateinit var mainWindow: BrowserWindow

fun createWindow() {
    mainWindow = BrowserWindow(
        jsObject {
            width = 800
            height = 800
            webPreferences = jsObject {
                nodeIntegration = false
                preload = path.join(__dirname, "../preloadProcess/FETerm-preload.js")
            }
        }
    ).apply {
        loadFile("index.html")
        webContents.openDevTools()
    }
}

fun main() {
    require("electron-reloader").unsafeCast<(NodeModule) -> Unit>()(module)
    val electron = require("electron").unsafeCast<dynamic>()
    val app2 = electron.app.unsafeCast<App>()
    app2.whenReady().then {
        createWindow()
        console.log(path.join(__dirname, "preload.js"))

        app2.on("activate") {
            // macOS では、Dock アイコンのクリック時に他に開いているウインドウがない
            // 場合、アプリのウインドウを再作成するのが一般的です。
            if (BrowserWindow.getAllWindows().isEmpty()) createWindow()
            console.log("wkara")
        }
    }

    app2.on("window-all-closed") {
        if (process.platform != "darwin") app2.quit()
    }
}
