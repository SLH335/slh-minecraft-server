package xyz.hafemann.slhserver.command

import xyz.hafemann.slhserver.util.sendTranslated

class PingCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage()

    syntax {
        sender.sendTranslated("command.ping.response", player.latency)
    }.requirePlayer()
})