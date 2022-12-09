import fs.Stats
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import ext.electron.contextBridge
import ext.electron.ipcRenderer
import kotlin.time.measureTime


fun main() {
    window.addEventListener("DOMContentLoaded",
        fun(_) {
            fun replaceText(selector: String, text: String) {
                document.getElementById(selector)?.innerHTML = text
            }
            arrayOf("chrome", "node", "electron").forEach {
                replaceText("$it-version", process.versions.asDynamic()[it].toString())
            }
        }
    )

    // contextBridge.exposeInMainWorld("ipcRenderer", ipcRenderer)
    contextBridge.apply {
        exposeInMainWorld("getWhatLocal", ::getWhatLocal)
        exposeInMainWorld("getRawFstatListLocal", ::getRawFstatListLocal)
        exposeInMainWorld("sendToTty", ::sendToTty)
        exposeInMainWorld("onFromTty", ::onFromTty)
    }
}

fun sendToTty(id: Int, param: String) {
    ipcRenderer.send("to-tty", id, param)
}

fun onFromTty(listener: (Event, Int, String) -> Unit) {
    ipcRenderer.on("from-tty", listener)
}

fun getWhatLocal(name: String): MutableList<String> {

    val ret = mutableListOf<String>()
    for (index in 0..5) {
        ret += (name + index.toString())
    }
    return ret
}

//fun getRawFstatLocal(fullPathFileName: String): Stats {
//    val rawFstat = fs.lstatSync(fullPathFileName)
//    rawFstat.asDynamic()["basename"] = path.basename(fullPathFileName)
//    rawFstat.asDynamic()["dirname"] = path.dirname(fullPathFileName)
//    rawFstat.asDynamic()["extname"] = path.extname(fullPathFileName)
//    return rawFstat
//}


fun getRawFstatListLocal(fullPathDirName: String): Array<dynamic> {
    val rawFstatArray = arrayOf<dynamic>()
    fs.readdirSync(fullPathDirName, "utf8").forEach {
        console.log(it)
    }
//    var rawFstatArray = arrayOf()
//    for (let fileName of fs.readdirSync(fullPathDirName)) {
//        rawFstatArray.push(getRawFstatLocal(path.join(fullPathDirName, fileName)))
//    }
    return rawFstatArray
}


//contextBridge.exposeInMainWorld("ipcRenderer", ipcRenderer)
//contextBridge.exposeInMainWorld("getRawFstatLocal", getRawFstatLocal)
//contextBridge.exposeInMainWorld("getRawFstatListLocal", getRawFstatListLocal)
//contextBridge.exposeInMainWorld("MainProcUserIDPackage", userid)
//contextBridge.exposeInMainWorld("MainProcPathPackage", path)
//
//contextBridge.exposeInMainWorld("fileSizeSI", fileSizeSI)
//
//contextBridge.exposeInMainWorld("createRemoteMachine", createRemoteMachine)


/*
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
