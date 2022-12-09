import NodeJS.Dict
import NodeJS.get
import ext.electron.App
import ext.electron.BrowserWindow
import ext.electron.ipcMain
import org.w3c.dom.events.Event
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

fun toTty(event: Event, id: Int, param: String) {
    console.log(event)
    console.log(id)
    console.log(param)
    fromTty()
}

fun fromTty() {
    mainWindow.webContents.send("from-tty", 1, "hoem")
}

fun main() {
    require("electron-reloader").unsafeCast<(NodeModule) -> Unit>()(module)
    val electron = require("electron").unsafeCast<dynamic>()
    val app2 = electron.app.unsafeCast<App>()
    app2.whenReady().then {
        createWindow()
        ipcMain.on("to-tty", ::toTty)
        app2.on("activate") {
            // macOS では、Dock アイコンのクリック時に他に開いているウインドウがない
            // 場合、アプリのウインドウを再作成するのが一般的です。
            if (BrowserWindow.getAllWindows().isEmpty()) createWindow()
        }

        val shell = if (os.platform() == "win32") "powershell.exe" else "bash"
        val ptyProcess = ext.nodePty.spawn(shell, arrayOf<String>(),
            jsObject {
                name = "xterm-color"
                cols = 80
                rows = 30
                cwd = process.env["HOME"]
                env = process.env
            });
        // console.log(ptyProcess)
        ptyProcess.onData{
            console.log(it)
        }
        ptyProcess.write("ls -l\n")
        ptyProcess.resize(40, 40)
        ptyProcess.write("ls -\n")
    }

    app2.on("window-all-closed") {
        if (process.platform != "darwin") app2.quit()
    }
}
