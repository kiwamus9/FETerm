package kotlinCommon.common

//inline fun jsObject(init: dynamic.() -> Unit): dynamic {
//    val o = js("{}")
//    init(o)
//    return o
//}

inline fun <T> jsOptions(init: T.() -> Unit): T {
    val o = js("{}") as T
    init(o)
    return o
}
