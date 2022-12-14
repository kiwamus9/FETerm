@file:JsModule("xterm")
@file:JsNonModule

package kotlinCommon.common.xterm

import org.w3c.dom.HTMLElement
import kotlin.js.Promise

@Suppress( "unused") // renderで使っている
external class Terminal {
    fun open(element: HTMLElement)
    fun <T> on(eventType: String, listener: (data:String) -> Promise<T>)
    fun write(text: String)
    fun onData(listener: (data:String) -> Unit)
}

//toCanvas(document.getElementById("t1")!!).then {
//    document.body?.appendChild(it)
//}
