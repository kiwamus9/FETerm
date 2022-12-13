@file:Suppress("unused")// lens だから

package components

import ContextMenuItem
import dev.fritz2.core.Lens
import dev.fritz2.core.lens
import ItemAlign

fun ContextMenuItem.Companion.label(): Lens<ContextMenuItem, String> = lens(
    "label",
    { it.label },
    { p, v -> p.copy(label = v) }
)

fun ContextMenuItem.Companion.divID(): Lens<ContextMenuItem, String> = lens(
    "divID",
    { it.divID },
    { p, v -> p.copy(divID = v) }
)

fun ContextMenuItem.Companion.divFlexBasis(): Lens<ContextMenuItem, Int> = lens(
    "divFlexBasis",
    { it.divFlexBasis },
    { p, v -> p.copy(divFlexBasis = v) }
)

fun ContextMenuItem.Companion.isShow(): Lens<ContextMenuItem, Boolean> = lens(
    "isShow",
    { it.isShow },
    { p, v -> p.copy(isShow = v) }
)

fun ContextMenuItem.Companion.isSortKey(): Lens<ContextMenuItem, Boolean> = lens(
    "isSortKey",
    { it.isSortKey },
    { p, v -> p.copy(isSortKey = v) }
)

fun ContextMenuItem.Companion.sortToBigger(): Lens<ContextMenuItem, Boolean> = lens(
    "sortToBigger",
    { it.sortToBigger },
    { p, v -> p.copy(sortToBigger = v) }
)

fun ContextMenuItem.Companion.itemAlign(): Lens<ContextMenuItem, ItemAlign> = lens(
    "itemAlign",
    { it.itemAlign },
    { p, v -> p.copy(itemAlign = v) }
)
