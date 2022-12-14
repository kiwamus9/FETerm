import NodeJS.get
import kotlinCommon.node.nodePty.SpawnOptions
import kotlinCommon.common.model.TerminalSession
import kotlinCommon.common.jsOptions
import kotlinCommon.node.electron.App
import kotlinCommon.node.electron.BrowserWindow
import kotlinCommon.node.electron.BrowserWindowOptions
import kotlinCommon.node.electron.ipcMain
import kotlinCommon.node.nodePty.spawn
import org.w3c.dom.events.Event
import path.path

lateinit var mainWindow: BrowserWindow

fun createWindow() {
    mainWindow = BrowserWindow(
        jsOptions<BrowserWindowOptions> {
            width = 800
            height = 800
            webPreferences = jsOptions {
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
        app2.on("activate") {
            // macOS では、Dock アイコンのクリック時に他に開いているウインドウがない
            // 場合、アプリのウインドウを再作成するのが一般的です。
            if (BrowserWindow.getAllWindows().isEmpty()) createWindow()
        }

        ipcMain.on("terminal:sendText", ::terminalSendText)
        ipcMain.handle("terminal:create", ::terminalSessionCreate)
    }

    app2.on("window-all-closed") {
        if (process.platform != "darwin") app2.quit()
    }
}
