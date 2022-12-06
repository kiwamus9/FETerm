import kotlinx.browser.document


fun main() {
    console.log("@rere222")
}



fun preload2() {
    fun replaceText(selector: String, text: String) {
        document.getElementById(selector)?.innerHTML = text
    }
    arrayOf("chrome", "node", "electron").forEach {
        //replaceText(it, process.versions)
        console.dir(process.versions)
        console.log("nannde222")
    }
}



// Node.js の全 API は、プリロードプロセスで利用可能です。
// Chrome 拡張機能と同じサンドボックスも持っています。
/*
window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector)
        if (element) element.innerText = text
    }

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency])
    }
})

const path = require('path')
const os = require('os')
const fs = require("fs")
const {contextBridge, ipcRenderer} = require("electron")
// const {getFstatLocal} = require("./main.js")
const userid = require("userid")
const {createRemoteMachine} = require("./module/ssh")

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

function fileSizeSI(a,b,c,d,e){
    return Number((b=Math,c=b.log,d=1e3,e=c(a)/c(d)|0,a/b.pow(d,e)).toFixed(2)).toString()
    +' '+(e?'KMGTPEZY'[--e]+'B':'Bytes')
}

console.dir(createRemoteMachine())

contextBridge.exposeInMainWorld("ipcRenderer", ipcRenderer)
contextBridge.exposeInMainWorld("getRawFstatLocal", getRawFstatLocal)
contextBridge.exposeInMainWorld("getRawFstatListLocal", getRawFstatListLocal)
contextBridge.exposeInMainWorld("MainProcUserIDPackage", userid)
contextBridge.exposeInMainWorld("MainProcPathPackage", path)

contextBridge.exposeInMainWorld("fileSizeSI", fileSizeSI)

contextBridge.exposeInMainWorld("createRemoteMachine", createRemoteMachine)
*/
