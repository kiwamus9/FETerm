package components

import customSvg
import dev.fritz2.core.RenderContext
import dev.fritz2.core.SvgTag

fun RenderContext.svgFileIcon(
    iconSelectorId: String,
    svgFileName: String,
    baseClass: String? = "",
    height: String = "16",
    width: String = "16",
    viewBox: String = "0 0 16 16",
    content: SvgTag.() -> Unit
): SvgTag {
    return svg(baseClass) {
        attr("xmlns", "http://www.w3.org/2000/svg")
        attr("fill", "currentColor")
        attr("viewBox", viewBox)
        attr("height", height)
        attr("width", width)
        customSvg("use") {
            attr("href", "${svgFileName}#${iconSelectorId}")
        }
        content()
    }
}

fun RenderContext.bootstrapIcon(
    iconSelectorId: String,
    baseClass: String? = "",
    height: String = "16",
    width: String = "16",
    viewBox: String = "0 0 16 16",
    content: SvgTag.() -> Unit
): SvgTag {
    return svgFileIcon(iconSelectorId, "icon/bootstrap-icons.svg", baseClass, height, width, viewBox, content)
}
