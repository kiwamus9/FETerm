package components

import ContextMenuItem

import FinderHeaderItemStore
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import kotlinx.browser.document
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.w3c.dom.events.Event

data class FinderHeaderContextMenuStatus(val isOpen: Boolean = false, val x: Int = 0, val y: Int = 0)

object FinderHeaderContextMenuStore :
    RootStore<FinderHeaderContextMenuStatus>(FinderHeaderContextMenuStatus(false, 0, 0)) {
    val close = handle { oldStatus ->
        FinderHeaderContextMenuStatus(false, oldStatus.x, oldStatus.y)
    }
    val open = handle { oldStatus ->
        FinderHeaderContextMenuStatus(true, oldStatus.x, oldStatus.y)
    }
    val openAt = handle { _, position: Pair<Int, Int> ->
        FinderHeaderContextMenuStatus(true, position.first, position.second)
    }
}

fun RenderContext.finderHeaderContextMenu(menuItemStore: FinderHeaderItemStore) {
    ul("finderHeaderContextMenu") {
        className(FinderHeaderContextMenuStore.data.map {
            if (it.isOpen) "show" else ""
        })
        inlineStyle(FinderHeaderContextMenuStore.data.map {
            "top: ${it.y}px; left: ${it.x}px"
        })

        // メニュー外でのクリックでメニューをクローズ
        val closeFunction = { _: Event -> FinderHeaderContextMenuStore.close() }

        FinderHeaderContextMenuStore.data handledBy {
            if (it.isOpen) {
                document.addEventListener("click", closeFunction)
            } else {
                document.removeEventListener("click", closeFunction)
            }
        }
        // メニュー行のレンダリング
        // 「名前」は表示しない
        menuItemStore.data.map { it.filter { contextMenuItem -> contextMenuItem.divID != "FLCol_basename" } }
            .renderEach(into = this) { menuItem: ContextMenuItem ->
                li {
                    if (menuItem.isShow)
                        bootstrapIcon("check-lg") {}
                    else
                        svgFileIcon("blank-icon", "icon/blank.svg") {}
                    +menuItem.label
                    className(merge(mouseenters.map { true }, mouseleaves.map { false }).map { isEnter ->
                        if (isEnter) "selected" else ""
                    })
                    clicks handledBy {
                        FinderHeaderItemStore.change(
                            ContextMenuItem.isShow().set(menuItem, !menuItem.isShow)
                        )
                        FinderHeaderContextMenuStore.close()
                    }
                }
            }
    }
}
