import components.*
import dev.fritz2.core.*
import dev.fritz2.routing.routerOf
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import model.ContextMenuItem
import model.TerminalSession

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Window
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.math.abs

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}

object FinderHeaderItemStore : RootStore<List<ContextMenuItem>>(fileListHeadersItems.toList()) {
    val change = handle<ContextMenuItem> { oldList, changedItem ->
        oldList.replace(changedItem) { it.label == changedItem.label }
    }
    val reverseSort = handle<ContextMenuItem> { oldList, changedItem ->
        val newItem = ContextMenuItem.sortToBigger().set(changedItem, !changedItem.sortToBigger)
        oldList.replace(newItem) { it.label == newItem.label }
    }
    val setSortKey = handle<ContextMenuItem> { oldList, changedItem ->
        val oldKeyItem = ContextMenuItem.isSortKey().set(oldList.find { it.isSortKey }!!, false)
        val list = oldList.replace(oldKeyItem) { it.label == oldKeyItem.label }
        val newKeyItem = ContextMenuItem.isSortKey().set(changedItem, true)
        list.replace(newKeyItem) { it.label == newKeyItem.label }
    }
}

fun RenderContext.finderTitle(
    item: ContextMenuItem,
    isFirst: Boolean, //ファイル名欄
    isLast: Boolean,
    syncFlexBasis: DivFlexBasisStore,
): HtmlTag<HTMLDivElement> {
    val classMapStore =
        object : RootStore<Map<String, Boolean>>(mapOf("last-col" to isLast, "mouse-down" to false)) {
            val put = handle<Pair<String, Boolean>> { oldMap, newPair ->
                oldMap.mapValues { if (it.key == newPair.first) newPair.second else it.value }
            }
        }

    fun colResizer(pageX: Double, item: ContextMenuItem) {
        val initialFlexBasis: Int = item.divFlexBasis
        // 1.addEventListenerをremoveEventListenerするためには，なぜか変数化しないとだめ
        //   ::mouseUpではremoveできない
        // 2.mouseUpはリカーシブになっているので，無名変数を使って変数にいきなり代入するとエラーになるので
        //   無駄だけど，lateinitであとから代入
        val mouseMove = fun(e: Event) {
            val diffX = (e as MouseEvent).pageX - pageX
            syncFlexBasis.intSize((initialFlexBasis + diffX).toInt())
        }

        ContextMenuItem.isSortKey()
        lateinit var mouseUp: (Event) -> Unit
        mouseUp = fun(e: Event) {
            val diffX = (e as MouseEvent).pageX - pageX
            FinderHeaderItemStore.change(ContextMenuItem.divFlexBasis().set(item, (initialFlexBasis + diffX).toInt()))
            document.removeEventListener("mouseup", mouseUp)
            document.removeEventListener("mousemove", mouseMove)
        }

        document.addEventListener("mouseup", mouseUp)
        document.addEventListener("mousemove", mouseMove)
    }

    return div("finder-title") {
        inlineStyle(syncFlexBasis.data)
        classMap(classMapStore.data)
        draggable(true, "true")
        div("finder-title-label") { +item.label }
        div("finder-row-title-button") {
            if (item.isSortKey) {
                bootstrapIcon(
                    if (item.sortToBigger) "chevron-down" else "chevron-up", "button-icon"
                ) {}
            } else {
                svgFileIcon("blank-icon", "icon/blank.svg", "button-icon") {}
            }
            div(if (isLast) "non-drag-area" else "drag-area") {
                draggable(true, "true")
                dragstarts.stopPropagation() handledBy {
                    colResizer(it.pageX, item)
                    it.preventDefault()

                    // resizeのキッカケだけdrag startsでとって，あとはdocument.mouseUp&mouseMoveに全てを任せる
                    // 全てをdragにできるけど，キャンセル時にイメージが元の場所に戻る時間が無駄にかかる
                    // しかも．onDragsのdragEventの最後にカーソル位置が(0,0)のゴミがまざる．．
                }
                // finder-row-titleのイベントハンドラが着火しないように止める
                clicks.stopPropagation() handledBy {}
                mousedowns.stopPropagation() handledBy {}
            }
        }
        mousedowns handledBy {
            // コンテクストメニューの時は発火しないように．Finderがそういう挙動
            if (it.button == 0.toShort()) classMapStore.put("mouse-down" to true)
        }
        mouseups handledBy {
            classMapStore.put("mouse-down" to false)
        }
        clicks handledBy {
            if (item.isSortKey) {
                FinderHeaderItemStore.reverseSort(item)
                // TODO: sort
            } else {
                FinderHeaderItemStore.setSortKey(item)
                // TODO: sort
            }
            FileItemListStore.sort(Pair(item.divID, item.sortToBigger))

        }
        contextmenus handledBy {
            FinderHeaderContextMenuStore.openAt(Pair(it.clientX, it.clientY))
        }
    }
}

fun RenderContext.finderItemsColumnItem(
    listIndex: Int, text: String, isFirstColumn: Boolean,
    selectFileRange: SelectFileRangeStore,
): Tag<HTMLDivElement> {
    return div("finder-items-column-item") {
        if (isFirstColumn) {
            val itemData = FileItemListStore.current[listIndex]
            if (itemData.type == FileType.S_IFDIR) { // ディレクトリ
                bootstrapIcon("caret-right", "button-icon expandArrow", width = "11px", height = "11px") {
                    val isExpand = MutableStateFlow(itemData.isChildrenExpand)
                    className(isExpand.map { if (it) "expand" else "" })
                    clicks handledBy {
                        isExpand.value = if (!isExpand.value) {
                            FileItemListStore.expand(itemData)
                            true
                        } else {
                            FileItemListStore.shrink(itemData)
                            false
                        }
                    }
                }
                bootstrapIcon("folder", "button-icon", width = "11px", height = "11px") {}
            } else {
                svgFileIcon("blank-icon", "icon/blank.svg", "button-icon", width = "11px", height = "11px") {}
                bootstrapIcon("file", "button-icon", width = "11px", height = "11px") {}

            }
        }
        className(selectFileRange.data.map {
            if ((it.first != null && it.second != null) && (listIndex in (it.first!!..it.second!!))) "selected"
            else ""
        })

        +text
        clicks handledBy { mouseEvent ->
            if (mouseEvent.shiftKey) {
                selectFileRange.shiftClick(listIndex)
            } else {
                selectFileRange.click(listIndex)
            }
        }
    }
}

class DivFlexBasisStore(initData: String) : RootStore<String>(initData) {
    val intSize = handle<Int> { _, size ->
        "flex-basis: ${size}px;"
    }
}

class SelectFileRangeStore(initData: Pair<Int?, Int?>) : RootStore<Pair<Int?, Int?>>(initData) {
    val click = handle<Int> { _, clickIndex -> Pair(clickIndex, clickIndex) }
    val shiftClick =
        handle<Int> { oldPair, clickIndex ->
            when {
                oldPair.first == null -> Pair(0, clickIndex)
                oldPair.first!! > clickIndex -> Pair(clickIndex, oldPair.first)
                else -> Pair(oldPair.first, clickIndex)
            }
        }
}

class MapStore<K, V>(initData: Map<K, V>) : RootStore<Map<K, V>>(initData) {
    val put = handle<Pair<K, V>> { oldMap, newPair ->
        if (!oldMap.containsKey(newPair.first)) error("No contains key in MapStore()")
        oldMap.mapValues { if (it.key == newPair.first) newPair.second else it.value }
    }
    val minus = handle<K> { oldMap, removeKey ->
        oldMap.minus(removeKey)
    }
    val plus = handle<Pair<K, V>> { oldMap, addPair ->
        oldMap.plus(addPair)
    }
}

data class ScrollSize(val clientSize: Double, val scrollSize: Double) {
    val minScrollPosition: Double = 0.0
    val maxScrollPosition: Double = scrollSize - clientSize
}

fun scrollPage(
    pages: Int,
    clientSize: Double,
    scrollPosition: MutableStateFlow<Double>,
    scrollArea: RootStore<ScrollSize>,
) {
    scrollDelta(clientSize * pages.toDouble(), scrollPosition, scrollArea)
}

fun scrollDelta(deltaValue: Double, scrollPosition: MutableStateFlow<Double>, scrollArea: RootStore<ScrollSize>) {
    if (deltaValue == 0.0) return
    val newValue = scrollPosition.value + deltaValue
    scrollPosition.value = when {
        (newValue <= scrollArea.current.minScrollPosition) -> scrollArea.current.minScrollPosition
        (newValue >= scrollArea.current.maxScrollPosition) -> scrollArea.current.maxScrollPosition
        else -> newValue
    }
}

//val classMapStore =
//    object : RootStore<Map<String, Boolean>>(mapOf("last-col" to isLast, "mouse-down" to false)) {
//        val put = handle<Pair<String, Boolean>> { oldMap, newPair ->
//            oldMap.mapValues { if (it.key == newPair.first) newPair.second else it.value }
//        }
//    }

fun main() {
//    val xxx = object {}.asDynamic()
//    xxx["canvasWidth"] = 100
//    xxx["canvasHeight"] = 100
//
//    console.dir(xxx)
//    toPng(document.getElementById("longMenu")!!).then {
//        val img = Image();
//        img.src = it;
//        document.body!!.appendChild(img);
//    }

    console.log("fff")

    console.log(window.getWhatLocal("kiwamu"))
    //kotlinext.js.require("bootstrap/dist/css/bootstrap.css")
    val xx = kotlinext.js.require("xterm/css/xterm.css")

    val currentDir = "/Users/kiwamu/Downloads"
    val fileItemDataList = FileItemDataList(currentDir)


    render("#target") {
        FileItemListStore.setup(fileItemDataList)
        val syncFlexBasisMap = buildMap {
            FinderHeaderItemStore.current.forEach {
                put(it.label, DivFlexBasisStore("flex-basis: ${it.divFlexBasis}px;"))
            }
        }
        // scroll
        val syncScrollLeftPosition = MutableStateFlow(0.0)
        val syncScrollTopPosition = MutableStateFlow(0.0)
        val horizontalScrollSize = object : RootStore<ScrollSize>(ScrollSize(0.0, 0.0)) {}
        val verticalScrollSize = object : RootStore<ScrollSize>(ScrollSize(0.0, 0.0)) {}
        // select
        val selectFileRange = SelectFileRangeStore(Pair(null, null))

        val router = routerOf("welcome")

        button {
            +"welcome"
            clicks handledBy { router.update("welcome") }
        }
        button {
            +"terminal"
            clicks handledBy { router.update("pageA") }
        }
        button {
            +"pageB"
            clicks handledBy { router.update("pageB") }
        }
        button {
            +"else"
            clicks handledBy { router.update("else") }
        }
        section {
            router.data.render { site ->
                when (site) {
                    "welcome" -> div("finder") {
                        // finder title context menu
                        finderHeaderContextMenu(FinderHeaderItemStore)
                        // finder body: Title
                        div("finder-titles-wrapper") {
                            div("finder-titles") {
                                syncScrollLeftPosition handledBy {
                                    this.domNode.unsafeCast<HTMLDivElement>().scrollLeft = it
                                }
                                FinderHeaderItemStore.data.map { list ->
                                    list.filter { it.isShow }
                                }.render(into = this) { list ->
                                    list.forEachIndexed { index, item ->
                                        finderTitle(
                                            item,
                                            (index == 0),
                                            (index == list.lastIndex),
                                            syncFlexBasisMap[item.label]!!
                                        ).afterMount { withDom, _ ->
                                            if (index == list.lastIndex) {
                                                val parentNode = withDom.domNode.parentElement
                                                horizontalScrollSize.update(
                                                    ScrollSize(
                                                        parentNode?.clientWidth?.toDouble() ?: 0.0,
                                                        parentNode?.scrollWidth?.toDouble() ?: 0.0
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        div("finder-items-scroll-wrapper") {
                            div("finder-items-wrapper-column") {
                                syncScrollLeftPosition handledBy {
                                    this.domNode.unsafeCast<HTMLDivElement>().scrollLeft = it
                                }
                                FinderHeaderItemStore.data.map { list ->
                                    list.filter { it.isShow }
                                }.render(into = this) {
                                    it.forEachIndexed { columnIndex, menuItem ->
                                        div("finder-items-column") {
                                            inlineStyle(syncFlexBasisMap[menuItem.label]!!.data)
                                            if (columnIndex == it.lastIndex) className("last-col")
                                            FileItemListStore.data.map { list ->
                                                list.map { listItem ->
                                                    listItem.getHumanizeString(
                                                        menuItem.divID,
                                                        "ja"
                                                    )
                                                }
                                            }.render(into = this) { stringList ->
                                                stringList.forEachIndexed { index, string ->
                                                    finderItemsColumnItem(
                                                        index,
                                                        string,
                                                        (columnIndex == 0),
                                                        selectFileRange
                                                    )
                                                        .afterMount { withDomTag, _ ->
                                                            if (index == stringList.lastIndex) {
                                                                val parentNode = withDomTag.domNode.parentElement
                                                                verticalScrollSize.update(
                                                                    ScrollSize(
                                                                        parentNode?.clientHeight?.toDouble() ?: 0.0,
                                                                        parentNode?.scrollHeight?.toDouble() ?: 0.0
                                                                    )
                                                                )
                                                            }
                                                        }
                                                }
                                            }
                                            syncScrollTopPosition handledBy { topPos ->
                                                this.domNode.unsafeCast<HTMLDivElement>().scrollTop = topPos
                                            }
                                        }.afterMount { withDomTag, _ ->
                                            val parentNode = withDomTag.domNode.parentElement
                                            horizontalScrollSize.update(
                                                ScrollSize(
                                                    parentNode?.clientWidth?.toDouble() ?: 0.0,
                                                    parentNode?.scrollWidth?.toDouble() ?: 0.0
                                                )
                                            )
                                        }
                                    }
                                }
                                wheels.stopPropagation() handledBy {
                                    if (abs(it.deltaX) >= abs(it.deltaY)) {
                                        scrollDelta(it.deltaX, syncScrollLeftPosition, horizontalScrollSize)
                                    } else {
                                        scrollDelta(it.deltaY, syncScrollTopPosition, verticalScrollSize)
                                    }
                                }
                            }
                            div("inner-scrollbar-horizontal") {
                                div("inner-scrollbar-horizontal-thumb") {
                                    inlineStyle(
                                        merge(horizontalScrollSize.data, syncScrollLeftPosition).map {
                                            val area = horizontalScrollSize.current
                                            "width: " + (this.domNode.parentElement!!.clientWidth * area.clientSize
                                                / area.scrollSize).toString() + "px; " +
                                                "left: " + (syncScrollLeftPosition.value * this.domNode.parentElement!!.clientWidth
                                                / area.scrollSize).toString() + "px;"
                                        }
                                    )
                                    mousedowns.stopPropagation().preventDefault() handledBy {
                                        windowLevelMouseMoveUp {
                                            scrollDelta(
                                                (it as MouseEvent).movementX * horizontalScrollSize.current.scrollSize
                                                    / this.domNode.parentElement!!.clientWidth,
                                                syncScrollLeftPosition,
                                                horizontalScrollSize
                                            )
                                        }
                                    }
                                    // スクロールバー上のクリックは無視
                                    clicks.stopPropagation().preventDefault() handledBy {}
                                }
                                clicks.stopPropagation().preventDefault() handledBy {
                                    val thumb =
                                        this.domNode.querySelector(".inner-scrollbar-horizontal-thumb")
                                            .unsafeCast<HTMLDivElement>()
                                    val page = when {
                                        it.clientX >= thumb.offsetLeft -> 1
                                        it.clientX < (thumb.offsetLeft + thumb.offsetWidth) -> -1
                                        else -> 0
                                    }
                                    scrollPage(
                                        page,
                                        this.domNode.clientWidth.toDouble(),
                                        syncScrollLeftPosition,
                                        horizontalScrollSize
                                    )
                                }
                            }
                            div("inner-scrollbar-vertical") {
                                div("inner-scrollbar-vertical-thumb") {
                                    inlineStyle(
                                        merge(verticalScrollSize.data, syncScrollTopPosition).map {
                                            val area = verticalScrollSize.current
                                            "height: " + (this.domNode.parentElement!!.clientHeight * area.clientSize
                                                / area.scrollSize).toString() + "px; " +
                                                "top: " + (syncScrollTopPosition.value * this.domNode.parentElement!!.clientHeight
                                                / area.scrollSize).toString() + "px;"
                                        }
                                    )
                                    mousedowns.stopPropagation().preventDefault() handledBy {
                                        windowLevelMouseMoveUp {
                                            scrollDelta(
                                                (it as MouseEvent).movementY * verticalScrollSize.current.scrollSize
                                                    / this.domNode.parentElement!!.clientHeight,
                                                syncScrollTopPosition,
                                                verticalScrollSize
                                            )
                                        }
                                    }
                                    // スクロールバー上のクリックは無視
                                    clicks.stopPropagation().preventDefault() handledBy {}
                                }
                                clicks.stopPropagation().preventDefault() handledBy {
                                    val thumb =
                                        this.domNode.querySelector(".inner-scrollbar-vertical-thumb")
                                            .unsafeCast<HTMLDivElement>()
                                    val page = when {
                                        it.clientY >= thumb.offsetTop -> 1
                                        it.clientY < (thumb.offsetTop + thumb.offsetHeight) -> -1
                                        else -> 0
                                    }
                                    scrollPage(
                                        page,
                                        this.domNode.clientHeight.toDouble(),
                                        syncScrollTopPosition,
                                        verticalScrollSize
                                    )
                                }
                            }
                        }
                    }

                    "terminal" -> renderTerminal()
                    "pageB" -> div {
                        div(id = "term") {}
                        button {
                            +"boeee"
                            clicks handledBy {
                                //window.sendToTty(99, "kiwamus9")
                                window.terminalCreate(
                                    jsObject {
                                        dnsName = "local"
                                        passwd = "kiwamu"
                                        terminalType = "xterm-256color"
                                    }).then { terminalSession ->
                                    val term = Terminal()
                                    term.open(document.getElementById("term") as HTMLDivElement)
                                    term.onData { text ->
                                        window.terminalSendText(terminalSession.sessionID, text)
                                    }
                                    window.terminalGetText { event, id, text ->
                                        term.write(text)
                                    }
                                }

                            }
                        }
                    }

                    else -> button {
                        +"mmimi"
                        clicks handledBy {
                            console.log("hoe?")
                            val hoe = document.getElementsByClassName("finder-row-items").asList()
                            hoe.forEach {
                                it.setAttribute("scrollTop", "40.0")
                            }
                        }
                    }
                }
            }
        }
    }
}


