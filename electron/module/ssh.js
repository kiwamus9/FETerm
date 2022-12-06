const {readFileSync} = require('fs');

const {Client} = require('ssh2');

let connetion = {
    host: "munch.stq.mydns.jp", port: 22, username: "kiwamu",
    privateKey: readFileSync("/Users/kiwamu/.ssh/id_rsa"),
}


const RemoteMachine = function () {
    this.conn = new Client()
    this.isConnecting = false
    this.isTimeout = "kiwa"
    this.error = null
}

RemoteMachine.prototype = {
    connect: function (connection_setting, call_back) {
        // this.conn.on("error", (error) => {
        //     this.error = error
        //     console.log(error)
        // })
        console.log(this.isConnecting)
        this.conn.on("ready", () => {
            this.isConnecting = true
            // call_back()
        })

        connection_setting["privateKey"] = readFileSync(connection_setting["privateKey"])
        // this.conn.connect(connection_setting)
    },
}


module.exports.createRemoteMachine = function () {

    let x = new RemoteMachine()
    console.log(x)
    return x
}




