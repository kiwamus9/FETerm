package components

import org.w3c.dom.Element

/*
htmlには，以下を書いただけ
<script src="js/simplebar.min.js"></script>
<link rel="stylesheet" href="css/simplebar.css"/>
*/

external class SimpleBar(element: Element, params: dynamic = definedExternally) {
    fun recalculate(): Unit
    fun getScrollElement(): Element
}
