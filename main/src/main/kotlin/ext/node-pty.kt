@file:JsModule("node-pty")

package ext.nodePty

// external val pty: NodePty
external fun spawn(shell: String, args: Array<String>, options: dynamic = definedExternally): IPty

external interface IPty {
    fun onData(listener: (String) -> Unit)
    fun resize(cons: Int, rows: Int)
    fun write(string: String)
}
