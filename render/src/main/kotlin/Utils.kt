import FileItemListStore.job
import dev.fritz2.core.*
import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.dom.Window
import org.w3c.dom.css.CSSStyleRule
import org.w3c.dom.css.CSSStyleSheet
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent

fun RenderContext.openEvalScope(context: (ScopeContext.() -> Unit)): Scope {
    return ScopeContext(this.scope).apply(context).scope
}

fun RenderContext.customSvg(
    tagName: String,
    baseClass: String? = null,
    id: String? = null,
    scope: (ScopeContext.() -> Unit) = {},
    content: SvgTag.() -> Unit
): SvgTag =
    register(SvgTag(tagName, id, baseClass, job = job, openEvalScope(scope)), content)


val <T : EventTarget> WithEvents<T>.pointerdown get() = subscribe<PointerEvent>("pointerdown")
val <T : EventTarget> WithEvents<T>.pointerup get() = subscribe<PointerEvent>("pointerup")
val <T : EventTarget> WithEvents<T>.pointermove get() = subscribe<PointerEvent>("pointermove")

fun <T : Element> Tag<T>.afterMountReturnElem(
    payload: Any? = null,
    handler: suspend (WithDomNode<Element>, Any?) -> Unit
): Tag<T> {
    this.afterMount(payload, handler)
    return this
}

val MouseEvent.movementX: Double get() = this.asDynamic()["movementX"] as Double
val MouseEvent.movementY: Double get() = this.asDynamic()["movementY"] as Double

fun windowLevelMouseMoveUp(upFunc: (Event) -> Unit? = fun(_) {}, moveFunc: (Event) -> Unit) {
    // 1.addEventListenerをremoveEventListenerするためには，なぜか変数化しないとだめ
    //   ::wrappedUpFuncではremoveできない
    // 2.wrappedUpFuncはリカーシブになっているので，無名変数を使って変数にいきなり代入するとエラーになるので
    //   無駄だけど，lateinitであとから代入
    lateinit var wrappedUpFunc: (Event) -> Unit
    wrappedUpFunc = fun(e:Event): Unit {
        upFunc(e)
        document.removeEventListener("mouseup", wrappedUpFunc)
        document.removeEventListener("mousemove", moveFunc)
    }
    document.addEventListener("mouseup", wrappedUpFunc)
    document.addEventListener("mousemove", moveFunc)
}

fun dynamicSetCssProperty(cssFileName: String, selectorText: String, styleText: String, valueText: String) {
    val styleSheet = (document.styleSheets.asList()
        .filter { it.href?.endsWith(cssFileName) ?: false }).firstOrNull()?.unsafeCast<CSSStyleSheet>()
        ?: error("No such CssFile")
    val cssRule = (styleSheet.cssRules.asList()
        .filter { (it as CSSStyleRule).selectorText == selectorText }).firstOrNull() as? CSSStyleRule
        ?: error("No such SelectorText")
    cssRule.asDynamic().style[styleText] = valueText
}

inline fun Window.getRawFstatLocal(fullPathFileName: String): Stat =
    asDynamic().getRawFstatLocal(fullPathFileName).unsafeCast<Stat>()

inline fun Window.getRawFstatListLocal(fullPathDirName: String): Array<Stat> =
    asDynamic().getRawFstatListLocal(fullPathDirName).unsafeCast<Array<Stat>>()

inline fun Window.fileSizeSI(fileSize: Long): String =
    asDynamic().fileSizeSI(fileSize).unsafeCast<String>()

inline fun Window.getWhatLocal(name: String): MutableList<String> =
    asDynamic().getWhatLocal(name).unsafeCast<MutableList<String>>()

//inline fun Window.createRemoteMachine():RemoteMachine =
//    asDynamic().createRemoteMachine().unsafeCast<RemoteMachine>()

external interface UserIDPackage {
    fun uid(userName: String): Int
    fun gid(groupName: String): Int
    fun username(uid: Int): String
    fun groupname(gid: Int): String
}

val mainProcUserIDPackage = js("window.MainProcUserIDPackage").unsafeCast<UserIDPackage>()

external interface PathPackage {
    fun basename(fullPathFileName: String): String
    fun dirname(fullPathFileName: String): String
    fun extname(fullPathFileName: String): String
    fun join(vararg paths: String): String
    val sep: String
}

object MainProcPathPackage :
    PathPackage by js("window.MainProcPathPackage").unsafeCast<PathPackage>() {
    fun dirDepthCalc(fullPathName: String):Int  {
        val sp = fullPathName.split(sep)
        return if(sp[1].isBlank()) return 1 else sp.size
    }
}

fun Document.htmlElement(id: String): HTMLElement {
    return this.getElementById(id) as? HTMLElement ?: error("Can't find Element Id($id)")
}

fun <T> List<T>.changedAt(index: Int, value: T): List<T> {
    return buildList {
        this@changedAt.forEachIndexed { i, t ->
            add(if (i == index) value else t)
        }
    }
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}
