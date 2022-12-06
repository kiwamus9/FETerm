// main.js
// import installExtension, { REACT_DEVELOPER_TOOLS } from 'electron-devtools-installer'
const {default: installExtension, REACT_DEVELOPER_TOOLS} = require('electron-devtools-installer');

// アプリケーションの寿命の制御と、ネイティブなブラウザウインドウを作成するモジュール
const {app, session, BrowserWindow, ipcMain, ipcRenderer} = require('electron')
const path = require('path')
const os = require('os')
const fs = require("fs")

// const reactDevToolsPath = path.join(
//     os.homedir(),
//     '/Library/Application Support/Google/Chrome/Profile 1/Extensions/fmkadmapgofadopljbjfkapdkoienihi/4.24.0_0'
// )
require('electron-reloader')(module)


const createWindow = () => {
    // ブラウザウインドウを作成します。
    const mainWindow = new BrowserWindow({
        width: 250,
        height: 400,
        webPreferences: {
            nodeIntegration: false,
            preload: path.join(__dirname, 'preload.js'),
        },
    })

    // そしてアプリの index.html を読み込みます。
    mainWindow.loadFile('index.html')

    // installExtension(REACT_DEVELOPER_TOOLS, {loadExtensionOptions: {allowFileAccess: true}, forceDownload: true})
    //     .then((name) => console.log(`Added Extensions: ${name}`))
    //     .catch((err) => console.log('An Error occurred: ', err))

// デベロッパー ツールを開きます。
    mainWindow.webContents.openDevTools()
}


// このメソッドは、Electron の初期化が完了し、
// ブラウザウインドウの作成準備ができたときに呼ばれます。
// 一部のAPIはこのイベントが発生した後にのみ利用できます。
app.whenReady().then(() => {
    // console.log("kiwawaf1")
    //
    // await session.defaultSession.loadExtension(reactDevToolsPath)
    // console.log("kiwawaf2")

    console.log("hoehoe")
    createWindow()
    console.log("hoehoe2")


    app.on('activate', () => {
        // macOS では、Dock アイコンのクリック時に他に開いているウインドウがない
        // 場合、アプリのウインドウを再作成するのが一般的です。
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })

    ipcMain.on("s-m", (event, args) => {
        console.log(args)
        event.returnValue = "pong"
    })
})

// macOS を除き、全ウインドウが閉じられたときに終了します。 ユーザーが
// Cmd + Q で明示的に終了するまで、アプリケーションとそのメニューバーを
// アクティブにするのが一般的です。
app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit()
})

// このファイルでは、アプリ内のとある他のメインプロセスコードを
// インクルードできます。
// 別々のファイルに分割してここで require することもできます。

