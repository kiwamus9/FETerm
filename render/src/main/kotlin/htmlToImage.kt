@file:JsModule("html-to-image")
@file:JsNonModule
@file:Suppress("unused")

import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise


external fun toPng(elem: Element, params: dynamic = definedExternally): Promise<String> // data:image/png;base64
external fun toCanvas(elem: Element,  params: dynamic = definedExternally): Promise<HTMLCanvasElement>

//toCanvas(document.getElementById("t1")!!).then {
//    document.body?.appendChild(it)
//}
