package com.github.krgermax.data.mineworld;

import com.github.krgermax.buttons.ButtonManager;
import com.github.krgermax.data.MobSpawner;
import com.github.krgermax.data.biomes.Biome;
import com.github.krgermax.data.biomes.BiomeType;
import com.github.krgermax.data.items.Item;
import com.github.krgermax.data.mobs.Mob;
import com.github.krgermax.main.Main;
import com.github.krgermax.tokens.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Mineworld {

    private LocalDateTime timestamp;
    /*
        Mob spawner
     */
    private final MobSpawner mobSpawner;

    /*
        Biome
     */
    private final List<Biome> biomeList;
    private Biome currentBiome;
    private BiomeType previousBiomeType;

    /*
        Current user map
     */
    private final Map<String, LocalDateTime> currentUserMap;
    private int currentUserMultiplier;
    private final int MAX_MINE_NOT_INTERACTED_MINUTES = 2;


    public Mineworld(List<Biome> biomeList, List<Mob> mobList) {
        this.timestamp = LocalDateTime.now();

        this.biomeList = biomeList;
        this.currentBiome = generateBiome();
        this.previousBiomeType = BiomeType.VOID;

        this.currentUserMap = new HashMap<>();
        this.currentUserMultiplier = 1;

        this.mobSpawner = new MobSpawner(this, mobList);
    }

    /**
     * Selects a random biom from the biomes enum
     *
     * @return A biom
     */
    private Biome generateBiome() {
        int size = biomeList.size();
        int selector = (int) (Math.random() * size);

        Biome returnBiome = biomeList.get(selector);
        Main.LOGGER.info("The current biome is: " + returnBiome.getType());
        /*
            The biome is copied to avoid shared mutable state between Mineworld instances
         */
        return returnBiome.copy();
    }

    private EmbedBuilder buildBiomeEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(currentBiome.getName())
                .setColor(Color.BLACK)
                .setDescription("Active miners: " + currentUserMap.size() + " (Last " + MAX_MINE_NOT_INTERACTED_MINUTES + " minutes)")
                .addField("Biome HP", currentBiome.getCurrentHP() + "/" + currentBiome.getAdjustableFullHP(), false)
                .setImage("attachment://ore.png");
        return embedBuilder;
    }

    /**
     * This method replies to the '/biome' command, by building an embedded message
     * with all necessary information and buttons
     *
     * @param event Event
     */
    public void replyWithBiomeEmbedded(SlashCommandInteractionEvent event) {
        if (currentBiome.getType().equals(previousBiomeType)) {
            event.replyEmbeds(buildBiomeEmbed().build())
                    .addActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
        } else {
            event.replyEmbeds(buildBiomeEmbed().build())
                    .addFiles(FileUpload.fromData(currentBiome.getImgFile(), "ore.png"))
                    .addActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
            previousBiomeType = currentBiome.getType();
        }
    }

    /**
     * This method replies to the 'mine' button, by building an embedded message
     * with all necessary information and buttons. This method overloads the method
     * with the SlashCommandInteractionEvent event param
     *
     * @param event Event
     */
    public void replyWithBiomeEmbedded(ButtonInteractionEvent event) {
        if (currentBiome.getType().equals(previousBiomeType)) {
            event.replyEmbeds(buildBiomeEmbed().build())
                    .addActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
        } else {
            event.replyEmbeds(buildBiomeEmbed().build())
                    .addFiles(FileUpload.fromData(currentBiome.getImgFile(), "ore.png"))
                    .addActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
            previousBiomeType = currentBiome.getType();
        }
    }

    /**
     * Updates the embedded message on a 'ButtonInteractionEvent'
     *
     * @param event Event
     */
    private void updateBiomeMsg(ButtonInteractionEvent event) {
        if (currentBiome.getType().equals(previousBiomeType)) {
            event.editMessageEmbeds(buildBiomeEmbed().build())
                    .setActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
        } else {
            event.editMessageEmbeds(buildBiomeEmbed().build())
                    .setFiles(FileUpload.fromData(currentBiome.getImgFile(), "ore.png"))
                    .setActionRow(Button.primary(ButtonManager.MINE_BUTTON_ID, Constants.MINE_BUTTON_EMOJI))
                    .queue();
            previousBiomeType = currentBiome.getType();
        }
    }

    /**
     * Wrapper method to update the biome on a 'ButtonInteractionEvent'
     * and the embedded message, such that the current state is displayed
     * correctly
     *
     * @param event        Event
     * @param equippedItem The equipped user item
     */
    public void updateBiome(ButtonInteractionEvent event, Item equippedItem) {
        currentBiome.damage(equippedItem);
        mobSpawner.spawnMob(event.getChannel());
        if (currentBiome.getCurrentHP() <= 0) {
            // Reset the old biome and generate a new one
            previousBiomeType = BiomeType.VOID;
            currentBiome = generateBiome();
            updateBiomeOnCompletion(event);
            /*
             * We need to update the mob spawner if a biome is completed and only if it is completed
             * to update the spawnable mob list, avoids unnecessary list filtering calls
             */
            mobSpawner.updateSpawner();
            return;
        }
        updateBiomeMsg(event);
    }

    /**
     * Updates the biome state and embedded message on a 'ButtonInteractionEvent', if
     * the biome has been completed by reaching HP of <= 0
     *
     * @param event Event
     */
    private void updateBiomeOnCompletion(ButtonInteractionEvent event) {
        event.getMessage().delete().queue();
        /*
            Since this method is called after a new biome is generated, we need to adjust its HP according to the amount
            of interacting users
         */
        adjustCurrentBiomeHP();
        currentBiome.setCurrentHP(currentBiome.getAdjustableFullHP());
        replyWithBiomeEmbedded(event);
        Main.LOGGER.info("Updated biome because of completion.");
    }

    /**
     * Add a user if interacted with the 'mine' button to the currentUserMap with a timestamp
     *
     * @param userID User ID
     */
    public void updateCurrentUserMultiplication(String userID) {
        int oldMapSize = this.currentUserMap.size();
        this.currentUserMap.put(userID, LocalDateTime.now());
        updateCurrentUserMap();
        int newMapSize = this.currentUserMap.size();
        if (oldMapSize != newMapSize)
            adjustCurrentBiomeHP();
    }

    /**
     * Updates the currentUserMap by removing users from the list that did not interact with the 'mine' button longer
     * than some minutes ago
     */
    private void updateCurrentUserMap() {
        Iterator<Map.Entry<String, LocalDateTime>> iterator = this.currentUserMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, LocalDateTime> entry = iterator.next();
            LocalDateTime lastInteractionTime = entry.getValue();

            if (lastInteractionTime.isBefore(LocalDateTime.now().minusMinutes(MAX_MINE_NOT_INTERACTED_MINUTES))) {
                iterator.remove();
            }
        }
    }

    /**
     * Adjusts the biome HP depending on how many unique users interacted with the mine button in a specific time window
     * <l>
     * <li>
     * If another user interacts and the current user map increases in size: <br>
     * This method only increases the adjustable full HP, but not the current biome HP
     * </li>
     * <li>
     * If the current user map decreases: <br>
     * This method only decreases the current full HP, but not the adjustable full HP. As this happens in
     * {@link #updateBiomeOnCompletion}
     * </li>
     * </l>
     */
    private void adjustCurrentBiomeHP() {
        int previousUserMultiplier = this.currentUserMultiplier;
        this.currentUserMultiplier = currentUserMap.size();

        double adjustedHP = Main.generator.roundDouble(currentBiome.getTrueHP() * currentUserMultiplier, 1);
        /*
            If the user count increases I do not want to erase previous progress, therefore only max hp is adjusted
         */
        currentBiome.setAdjustableFullHP(adjustedHP);

        if (this.currentUserMultiplier < previousUserMultiplier) {
            double adjustment = (double) currentUserMultiplier / previousUserMultiplier;
            adjustedHP = Main.generator.roundDouble(currentBiome.getCurrentHP() * adjustment, 1);
            currentBiome.setCurrentHP(adjustedHP);
        }
    }

    public Biome getCurrentBiome() {
        return currentBiome;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
