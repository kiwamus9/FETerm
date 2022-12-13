@Suppress("unused") // TODO
enum class TerminalSessionType {
    Local, SSH, Telnet, Ftp
}

data class TerminalSession (
    val name: String = "local",
    val sessionID: Int = getSessionID(),
    val dnsName: String = "local",
    val port: Int? = null,
    val type: TerminalSessionType = TerminalSessionType.Local,
    var isConnect: Boolean = false,
    val terminalType: String = "dumb",
    val cols: Int = 0,
    val rows: Int = 0,
) {
    companion object {
        private var sessionIDSource: Int = 0
        fun getSessionID(): Int {
            return(++sessionIDSource)
        }
    }
}

@Suppress("unused")
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

