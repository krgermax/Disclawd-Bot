package com.github.krgermax.commands;

import com.github.krgermax.commands.type.slashcommand.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.github.krgermax.main.Main;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;

public class CommandManager extends ListenerAdapter {

    private final HashMap<String, SlashCommand> commands;

    /*
     * Command Id's
     */
    public static final String BIOME_COMMAND_ID = "biome";
    public static final String INV_COMMAND_ID = "inventory";
    public static final String SHOP_COMMAND_ID = "shop";
    public static final String ITEM_COMMAND_ID = "item";
    public static final String HELP_COMMAND_ID = "help";
    public static final String RANK_COMMAND_ID = "rank";

    /*
     * Command option Id's
     */
    public static final String ITEM_COMMAND_OPTION_ID = "name";
    public static final String RANK_COMMAND_OPTION_ID = "category";
    public static final String INV_COMMAND_OPTION_ID = "category";

    public CommandManager() {
        this.commands = new HashMap<>();

        this.commands.put(HELP_COMMAND_ID, new HelpCommand());
        this.commands.put(BIOME_COMMAND_ID, new BiomeCommand());
        this.commands.put(SHOP_COMMAND_ID, new ShopCommand());
        this.commands.put(ITEM_COMMAND_ID, new ItemCommand());
        this.commands.put(INV_COMMAND_ID, new InvCommand());
        this.commands.put(RANK_COMMAND_ID, new RankCommand());
    }

    /**
     * Receives slash command interaction event and calls on the
     * corresponding command class executeCommand() method
     *
     * @param event Received event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        Main.LOGGER.info("Received slash command: " + command);

        SlashCommand slashCommand = commands.get(command);
        if (slashCommand != null) {
            slashCommand.executeCommand(event);
        } else {
            Main.LOGGER.info("Command: " + command + ", does not exist.");
            replyToNonExistingCommand(event);
        }
    }

    /**
     * This function is called in the case of a non-existing slash command
     *
     * @param event Received event
     */
    private void replyToNonExistingCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(Color.RED);
        embedBuilder.setTitle("Command does not exist!");
        embedBuilder.setDescription("The command you are trying to use doesnt exist or was eaten" +
                " by the cookie monster :confused:");

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
