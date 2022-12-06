package model

import dev.fritz2.core.Lens
import dev.fritz2.core.Lenses
import dev.fritz2.core.lens

@Lenses
data class Name(val firstname: String, val lastname: String) {
    companion object
}

@Lenses
data class Person(val name: Name, var description: String) {
    companion object
}

@Lenses
data class TableHeaderItem(var label: String, var width: Double, var minWidth: Double) {
    companion object
}

@Lenses
data class Muu(val guu: String){
    companion object
}

enum class ItemAlign {
    Left, Center, Right
}

data class ContextMenuItem(
    val label: String,
    val divID: String,
    val divFlexBasis: Int,
    val isShow: Boolean,
    val isSortKey: Boolean,
    val sortToBigger: Boolean,
    val itemAlign: ItemAlign,
) {
    companion object
}


public fun ContextMenuItem.Companion.label(): Lens<ContextMenuItem, String> = lens(
    "label",
    { it.label },
    { p, v -> p.copy(label = v)}
)

public fun ContextMenuItem.Companion.divID(): Lens<ContextMenuItem, String> = lens(
    "divID",
    { it.divID },
    { p, v -> p.copy(divID = v)}
)

public fun ContextMenuItem.Companion.divFlexBasis(): Lens<ContextMenuItem, Int> = lens(
    "divFlexBasis",
    { it.divFlexBasis },
    { p, v -> p.copy(divFlexBasis = v)}
)

public fun ContextMenuItem.Companion.isShow(): Lens<ContextMenuItem, Boolean> = lens(
    "isShow",
    { it.isShow },
    { p, v -> p.copy(isShow = v)}
)

public fun ContextMenuItem.Companion.isSortKey(): Lens<ContextMenuItem, Boolean> = lens(
    "isSortKey",
    { it.isSortKey },
    { p, v -> p.copy(isSortKey = v)}
)

public fun ContextMenuItem.Companion.sortToBigger(): Lens<ContextMenuItem, Boolean> = lens(
    "sortToBigger",
    { it.sortToBigger },
    { p, v -> p.copy(sortToBigger = v)}
)

public fun ContextMenuItem.Companion.itemAlign(): Lens<ContextMenuItem, ItemAlign> = lens(
    "itemAlign",
    { it.itemAlign },
    { p, v -> p.copy(itemAlign = v)}
)
