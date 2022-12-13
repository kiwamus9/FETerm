@file:JsModule("node-pty")

package ext.nodePty

import NodeJS.ProcessEnv

// external val pty: NodePty
external interface SpawnOptions {
    var name: String
    var cols: Int
    var rows: Int
    var cwd: String?
    var env: ProcessEnv
}
external fun spawn(shell: String, args: Array<String>, options: dynamic = definedExternally): IPty

external interface IPty {
    fun onData(listener: (String) -> Unit)
    fun resize(cons: Int, rows: Int)
    fun write(string: String)
}
