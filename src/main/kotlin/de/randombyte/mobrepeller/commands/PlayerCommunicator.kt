package de.randombyte.mobrepeller.commands

import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

object PlayerCommunicator {
    fun CommandSource.fail(message: String): CommandResult {
        sendMessage(Text.of(TextColors.DARK_RED, message))
        return CommandResult.empty()
    }

    fun CommandSource.success(message: String): CommandResult {
        sendMessage(Text.of(TextColors.GREEN, message))
        return CommandResult.success()
    }
}