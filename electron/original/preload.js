// preload.js

// Node.js の全 API は、プリロードプロセスで利用可能です。
// Chrome 拡張機能と同じサンドボックスも持っています。



const path = require('path')
const os = require('os')
const fs = require("fs")
const {contextBridge, ipcRenderer} = require("electron")
// const {getFstatLocal} = require("./main.js")
const userid = require("userid")
const {createRemoteMachine} = require("../module/ssh")
const {preload2} = require("./preload/preload")
// const hoe = require('./main')
//
console.log(preload2)


window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector)
        if (element) element.innerText = text
    }

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency])
    }
})

function getRawFstatLocal(fullPathFileName) {
    const rawFstat = fs.lstatSync(fullPathFileName)
    rawFstat["basename"] = path.basename(fullPathFileName)
    rawFstat["dirname"] = path.dirname(fullPathFileName)
    rawFstat["extname"] = path.extname(fullPathFileName)
    return rawFstat
}

// Objectのままだとkotlinにもっていけない．Array:Javascript -> Array:kotlin
function getRawFstatListLocal(fullPathDirName) {
    let rawFstatArray = []
    for (let fileName of fs.readdirSync(fullPathDirName)) {
        rawFstatArray.push(getRawFstatLocal(path.join(fullPathDirName, fileName)))
    }
    return rawFstatArray
}

function fileSizeSI(a, b, c, d, e) {
    return Number((b = Math, c = b.log, d = 1e3, e = c(a) / c(d) | 0, a / b.pow(d, e)).toFixed(2)).toString()
        + ' ' + (e ? 'KMGTPEZY'[--e] + 'B' : 'Bytes')
}

console.dir(createRemoteMachine())

contextBridge.exposeInMainWorld("ipcRenderer", ipcRenderer)
contextBridge.exposeInMainWorld("getRawFstatLocal", getRawFstatLocal)
contextBridge.exposeInMainWorld("getRawFstatListLocal", getRawFstatListLocal)
contextBridge.exposeInMainWorld("MainProcUserIDPackage", userid)
contextBridge.exposeInMainWorld("MainProcPathPackage", path)

contextBridge.exposeInMainWorld("fileSizeSI", fileSizeSI)

contextBridge.exposeInMainWorld("createRemoteMachine", createRemoteMachine)
