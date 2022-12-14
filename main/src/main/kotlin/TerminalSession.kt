import NodeJS.get
import kotlinCommon.common.jsOptions
import kotlinCommon.common.model.TerminalSession
import kotlinCommon.node.nodePty.SpawnOptions
import kotlinCommon.node.nodePty.spawn
import org.w3c.dom.events.Event

/*
    terminal:create
    terminal:sendText
    terminal:getText
    terminal:sendData
    terminal:getData
    terminal:close

 */

interface TerminalSessionProcess {
    fun onData(listener: (String) -> Unit)
    fun resize(cols: Int, rows: Int)
    fun write(string: String)
}

object TerminalSessions {
    private val map = mutableMapOf<TerminalSession, TerminalSessionProcess>()

    fun add(terminalSession: TerminalSession, sessionProcess: TerminalSessionProcess) {
        map[terminalSession] = sessionProcess
    }

    fun get(terminalSession: TerminalSession): TerminalSessionProcess? {
        return map[terminalSession]
    }

    fun sessionProcess(id: Int): TerminalSessionProcess {
        return map[map.keys.first { it.sessionID == id }]!!
    }
}

class LocalTerminalSessionProcess(private var terminalSession: TerminalSession) : TerminalSessionProcess {
    private val shell: String = if (os.platform() == "win32") "powershell.exe" else "bash"
    private val ptyProcess = spawn(shell, arrayOf(),
        jsOptions<SpawnOptions> {
            name = terminalSession.terminalType
            cols = terminalSession.cols
            rows = terminalSession.rows
            cwd = process.env["HOME"]
            env = process.env
        })

    init {
        onData {
            mainWindow.webContents.send("terminal:getText", terminalSession.sessionID, it)
        }
    }

    override fun onData(listener: (String) -> Unit) {
        ptyProcess.onData(listener)
    }

    override fun resize(cols: Int, rows: Int) {
        ptyProcess.resize(cols, rows)
    }

    override fun write(string: String) {
        ptyProcess.write(string)
    }
}

fun terminalSessionCreate(@Suppress("UNUSED_PARAMETER") event: Event, param: dynamic): TerminalSession {
    //dnsName: String, passwd: String
    val dnsName = param["dnsName"] as String
    //val passwd = param["passwd"] as String
    val terminalType = param["terminalType"] as String

    val ts = TerminalSession(dnsName = dnsName, terminalType = terminalType)
    TerminalSessions.add(ts, LocalTerminalSessionProcess(ts))

    return ts
}

fun terminalSendText(@Suppress("UNUSED_PARAMETER") event: Event, id: Int, text: String) {
    TerminalSessions.sessionProcess(id).write(text)
}
