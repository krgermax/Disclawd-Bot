package com.github.krgermax.commands;

import com.github.krgermax.data.items.Item;
import com.github.krgermax.main.Main;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.stream.Stream;

public class AutoCompleteListener extends ListenerAdapter {

    /*
     * Command option values
     */
    public static final String MINED_OPTION_ID = "Mined";
    public static final String KILLS_OPTION_ID = "Kills";
    public static final String GOLD_OPTION_ID = "Gold";
    public static final String XP_OPTION_ID = "XP";

    public static final String INVENTORY_ITEM_OPTION_ID = "Items";
    public static final String INVENTORY_PET_OPTION_ID = "Pets";
    public static final String INVENTORY_DROPS_OPTION_ID = "Drops";

    private final String[] rankOptions = new String[]{
            MINED_OPTION_ID,
            XP_OPTION_ID,
            GOLD_OPTION_ID,
            KILLS_OPTION_ID,
    };

    private final String[] invOptions = new String[]{
            INVENTORY_ITEM_OPTION_ID,
            INVENTORY_PET_OPTION_ID,
            INVENTORY_DROPS_OPTION_ID,
    };

    /**
     * Handles auto complete interactions for commands
     *
     * @param event The CommandAutoCompleteInteractionEvent event
     */
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        /*
            Handles auto complete for the '/rank' command
         */
        if (event.getName().equals(CommandManager.RANK_COMMAND_ID) && event.getFocusedOption().getName().equals(CommandManager.RANK_COMMAND_OPTION_ID)) {
            List<Command.Choice> options = generateStringChoiceList(event, Stream.of(rankOptions).toList());
            event.replyChoices(options).queue();
        }
        /*
            Handles auto complete for the '/item' command
         */
        if (event.getName().equals(CommandManager.ITEM_COMMAND_ID) && event.getFocusedOption().getName().equals(CommandManager.ITEM_COMMAND_OPTION_ID)) {
            List<Command.Choice> options = generateItemChoiceList(event, Main.shopManager.getItemList());
            event.replyChoices(options).queue();
        }

        /*
            Handles auto complete for the '/inventory' command
         */
        if (event.getName().equals(CommandManager.INV_COMMAND_ID) && event.getFocusedOption().getName().equals(CommandManager.INV_COMMAND_OPTION_ID)) {
            List<Command.Choice> options = generateStringChoiceList(event, Stream.of(invOptions).toList());
            event.replyChoices(options).queue();
        }
    }

    /**
     * Generates a list of Command.Choice objects based on the provided strings
     *
     * @param event   The CommandAutoCompleteInteractionEvent event
     * @param strings The list of strings to filter
     * @return A list of Command.Choice objects
     */
    private List<Command.Choice> generateStringChoiceList(CommandAutoCompleteInteractionEvent event, List<String> strings) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();
        return strings.stream()
                .filter(s -> s.toLowerCase().startsWith(userInput))
                .map(s -> new Command.Choice(s, s))
                .limit(25)
                .toList();
    }

    /**
     * Generates a list of Command.Choice objects based on the provided Item objects
     *
     * @param event       The CommandAutoCompleteInteractionEvent event
     * @param dataObjects The list of Item objects to filter
     * @return A list of Command.Choice objects
     */
    private List<Command.Choice> generateItemChoiceList(CommandAutoCompleteInteractionEvent event, List<Item> dataObjects) {
        List<String> names = dataObjects.stream().map(Item::getName).toList();
        return generateStringChoiceList(event, names);
    }
}
