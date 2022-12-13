@file:JsModule("xterm")
@file:JsNonModule

import org.w3c.dom.HTMLElement

@Suppress( "unused") // renderで使っている
external class Terminal {
    fun open(element: HTMLElement)
    fun on(eventType: String, listener: (data:String) -> Unit)
    fun write(text: String)
    fun onData(listener: (data:String) -> Unit)
}

//toCanvas(document.getElementById("t1")!!).then {
//    document.body?.appendChild(it)
//}
