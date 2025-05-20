package com.github.krgermax.commands.type.slashcommand;

import com.github.krgermax.commands.CommandManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.github.krgermax.main.Main;

public class InvCommand implements SlashCommand {

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        String userID = event.getUser().getId();
        if (!Main.sqlHandler.isUserRegistered(userID)) {
            Main.sqlHandler.registerUser(userID);
        }
        // TODO check for inventory filter type, adjust inventory based on this type
        Main.inventoryManager.replyWithInventoryFirstEmbedded(event);
        Main.LOGGER.info("Executed '" + CommandManager.INV_COMMAND_ID + "' button");
    }
}
