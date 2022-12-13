import components.bootstrapIcon
import components.isShow
import components.sortToBigger
import components.svgFileIcon

import dev.fritz2.core.*

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import model.ContextMenuItem
import model.ItemAlign

import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

import kotlin.math.abs

data class ContextMenuStatus(val isOpen: Boolean = false, val x: Int = 0, val y: Int = 0)

val fileListHeadersItems =
    mutableListOf<ContextMenuItem>(
        //basename
        ContextMenuItem("名前", "FLCol_basename", 150,true, true, true, ItemAlign.Left),
        //mtime
        ContextMenuItem("変更日", "FLCol_mtime", 150,true, false, true, ItemAlign.Left),
        //birthTime
        ContextMenuItem("作成日", "FLCol_birthTime", 150,true, false, true, ItemAlign.Left),
        //aTime
        ContextMenuItem("最後に開いた日", "FLCol_aTime", 150,true, false, true, ItemAlign.Left),
        //cTime
        ContextMenuItem("追加日", "FLCol_cTime",   150,false, false, true, ItemAlign.Left),
        //size
        ContextMenuItem("サイズ", "FLCol_size", 150,true, false, true, ItemAlign.Right),
        //type
        ContextMenuItem("種類", "FLCol_type", 150,true, false, true, ItemAlign.Left),
        //mode
        ContextMenuItem("モード", "FLCol_mode", 150,false, false, true, ItemAlign.Right),
        //username
        ContextMenuItem("所有者", "FLCol_username",    150,false, false, true, ItemAlign.Right),
    )


fun mainOld() {
    console.log(getFileItemListLocal("/"))
    val fileListContextMenuItemStore = object : RootStore<List<ContextMenuItem>>(fileListHeadersItems.toList()) {
        public val change = handle<ContextMenuItem> { _, changedItem ->
            val index = fileListHeadersItems.indexOfFirst { it.label == changedItem.label }
            fileListHeadersItems[index] = changedItem
            fileListHeadersItems.toList()
        }
    }
    val fileContextMenuStore = object : RootStore<ContextMenuStatus>(ContextMenuStatus(false, 0, 0)) {
        val close = handle { oldStatus ->
            ContextMenuStatus(false, oldStatus.x, oldStatus.y)
        }
        val open = handle { oldStatus ->
            ContextMenuStatus(true, oldStatus.x, oldStatus.y)
        }
        val openAt = handle<Pair<Int, Int>> { _, position: Pair<Int, Int> ->
            ContextMenuStatus(true, position.first, position.second)
        }
    }

    val fileListStore = object : RootStore<List<FileItemData>>(listOf<FileItemData>()) {

    }

    fun RenderContext.fileListContextMenu(elementID: String): HtmlTag<HTMLUListElement> {
        return ul(id = elementID) {
            className(fileContextMenuStore.data.map {
                if (it.isOpen) "show" else ""
            })
            inlineStyle(fileContextMenuStore.data.map {
                "top: ${it.y}px; left: ${it.x}px"
            })

            // メニュー外でのクリックでメニューをクローズ
            val closeFunction = { _: Event -> fileContextMenuStore.close() }

            fileContextMenuStore.data handledBy {
                if (it.isOpen) {
                    document.addEventListener("click", closeFunction)
                } else {
                    document.removeEventListener("click", closeFunction)
                }
            }
            // メニュー行のレンダリング
            // 「名前」は表示しない
            fileListContextMenuItemStore.data.map { it.filter { contextMenuItem -> contextMenuItem.divID != "FLCol_basename" } }
                .renderEach(into = this) { menuItem: ContextMenuItem ->
                    li {
                        if (menuItem.isShow)
                            bootstrapIcon("check-lg") {}
                        else
                            svgFileIcon("blank-icon", "icon/blank.svg"){}
                        +menuItem.label
                        className(merge(mouseenters.map { true }, mouseleaves.map { false }).map { isEnter ->
                            if (isEnter) "selected" else ""
                        })
                        clicks handledBy {
                            fileListContextMenuItemStore.change(
                                ContextMenuItem.isShow().set(menuItem, !menuItem.isShow)
                            )
                            fileContextMenuStore.close()
                        }
                    }
                }
        }
    }

    val fileListColResizeListener = object {
        var nowResizing = false
        var elementClientLeft: Int = 0
        var elementClientWidth: Int = 0
        var pageOffsetX: Int = 0
        var cssSelectorText = ""

        // 変数化しないとremoveEventListenerできない．．
        // late initしないと，リカーシブなので，自分内で自分をremoveできない．．
        lateinit var mouseUp: (Event) -> Unit
        lateinit var mouseMove: (Event) -> Unit

        init {
            mouseMove = fun(event: Event) {
                val mouseEvent = event as MouseEvent
                val width = mouseEvent.pageX - (elementClientLeft + pageOffsetX)
                dynamicSetCssProperty("F2Term.css", ".$cssSelectorText", "width", "${width}px")
            }
            mouseUp = fun(_: Event) {
                document.body?.classList?.remove("cursor_col_resize")
                nowResizing = false
                document.removeEventListener("mouseup", mouseUp, true)
                document.removeEventListener("mousemove", mouseMove, true)
            }
        }

        fun startResize(cssSelectorText: String, domElement: HTMLSpanElement) {
            elementClientLeft = domElement.getBoundingClientRect().left.toInt()
            elementClientWidth = domElement.clientWidth
            pageOffsetX = window.pageXOffset.toInt()
            this.cssSelectorText = cssSelectorText

            document.body?.classList?.add("cursor_col_resize")

            nowResizing = true
            document.addEventListener("mouseup", mouseUp, true)
            document.addEventListener("mousemove", mouseMove, true)

        }
    }

    fun RenderContext.fileListHeader(menuItem: ContextMenuItem): HtmlTag<HTMLSpanElement> {
        return span(baseClass = menuItem.divID) {

            fun isMouseOnResizeEdge(mouseEvent: MouseEvent): Boolean {
                return abs(mouseEvent.offsetX - this.domNode.clientWidth) < 5
            }
            // 右エッジにマウスがきたらorリサイズ中は，リサイズカーソルに変更
            mousemoves handledBy { mouseEvent: MouseEvent ->
                // サイズ変更はFileListColResizeListenerに頼む
                inlineStyle(
                    if ((isMouseOnResizeEdge(mouseEvent)) || fileListColResizeListener.nowResizing) "cursor: col-resize" else "cursor: class"
                )
            }
            mousedowns handledBy { mouseEvent: MouseEvent ->
                if (isMouseOnResizeEdge(mouseEvent)) fileListColResizeListener.startResize(
                    menuItem.divID,
                    this.domNode
                )
            }
//            pointerdown handledBy { e: PointerEvent ->
//                console.log("sage?")
//                this.domNode.setPointerCapture(e.pointerId)
//            }
//            pointerup handledBy { e: PointerEvent ->
//                console.log("age?")
//                this.domNode.releasePointerCapture(e.pointerId)
//            }
            contextmenus handledBy {
                fileContextMenuStore.openAt(Pair(it.clientX, it.clientY))
            }

            //　ドラッグするとクリックからはずれるので，要素のソートの切り替えをハンドリング
            clicks handledBy {
                fileListContextMenuItemStore.change(
                    ContextMenuItem.sortToBigger().set(menuItem, !menuItem.sortToBigger)
                )
            }

            span {
                inlineStyle("float: left")
                +menuItem.label
            }
            span {
                inlineStyle("float: right")
                if (menuItem.sortToBigger)
                    bootstrapIcon("chevron-down") {}
                else
                    bootstrapIcon("chevron-up") {}
            }
        }
    }

    fun RenderContext.fileListHeaders() {
        li("FileListHeader") {
            fileListContextMenuItemStore.data.map {
                it.filter { contextMenuItem -> contextMenuItem.isShow }
            }.renderEach(into = this) {
                fileListHeader(it)
            }
        }
    }

    render("#target") { // by default target = document.body
        //Window.loads handledBy { console.log("reseize") }
        // dialog
        //fileListContextMenu("FileListItemContextMenu")

        console.dir(::toPng)



        // main
        ul("FileList") {
            fileListHeaders()
            fileListStore.data.renderEach() { fileItemData: FileItemData ->
                li("FileListItem") {
                    fileListContextMenuItemStore.data.map { it.filter { contextMenuItem -> contextMenuItem.isShow } }
                        .renderEach() { header: ContextMenuItem ->
                            span("${header.divID}") { +"aaaaaa" }
                        }
                }
            }

        }
        button {
            +"hoe"
            clicks handledBy {
                console.log("pupu")
                console.log((getFileItemListLocal("/")))
                fileListStore.update(getFileItemListLocal("/"))
            }
        }
    }
}
