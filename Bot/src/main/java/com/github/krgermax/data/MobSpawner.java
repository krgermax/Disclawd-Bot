package com.github.krgermax.data;

import com.github.krgermax.buttons.ButtonManager;
import com.github.krgermax.data.biomes.Biome;
import com.github.krgermax.data.mineworld.Mineworld;
import com.github.krgermax.data.mobs.Mob;
import com.github.krgermax.data.mobs.enums.MobSubType;
import com.github.krgermax.data.mobs.enums.MobType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import com.github.krgermax.main.Main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MobSpawner {

    private final Mineworld mineworld;
    private final List<Mob> mobList;
    private List<Mob> spawnableMobs = new ArrayList<>();
    private final Random RANDOM = new Random();  // TODO: might use a seed

    public MobSpawner(Mineworld mineworld, List<Mob> mobList) {
        this.mineworld = mineworld;
        this.mobList = mobList;
        updateSpawner();
    }

    /**
     * This method can be used to update the mob spawner such that the spawning mobs
     * match to the current state of the game. Note from the {@link Mineworld}
     * class: <br> We need to update the mob spawner if a biome is completed and only if it is
     * completed to update the spawnable mob list, avoids unnecessary list filtering calls
     */
    public void updateSpawner() {
        Biome currentBiome = mineworld.getCurrentBiome();
        this.spawnableMobs = generateSpawnableMobs(currentBiome);
    }

    /**
     * Generates a list of all spawnable mobs, cannot implement this function to return
     * a set because multiple mobs do share the same subtype with their rarer counterparts
     *
     * @param currentBiome The current biome influences the selection of mobs
     * @return The generated mob list
     */
    private List<Mob> generateSpawnableMobs(Biome currentBiome) {
        Set<MobSubType> spawnableMobTypes = currentBiome.getSpawnableMobSubTypes();
        return spawnableMobs = this.mobList.stream()
                .filter(mob -> spawnableMobTypes.contains(mob.getMobSubType()))
                .toList();
    }

    public void spawnMob(MessageChannelUnion messageChannelUnion) {
        Main.LOGGER.info("Attempting to spawn a mob.");
        attemptSpawn(messageChannelUnion, MobType.NORMAL);
        /*
         * TODO:
         *  - Adjust biomes those mob types arent entered anywhere yet
         *  - The function doesnt need to be called three times, could change that in
         *    combination with other changes
         */
        attemptSpawn(messageChannelUnion, MobType.TRADER);
        attemptSpawn(messageChannelUnion, MobType.BOSS);
    }

    /**
     * Wrapper function, holding a necessary steps to attempt to spawn a mob
     *
     * @param messageChannelUnion The channel the message should be sent to
     * @param mobType             (Maybe not needed later on)
     */
    private void attemptSpawn(MessageChannelUnion messageChannelUnion, MobType mobType) {
        List<Mob> mobs = this.spawnableMobs.stream()
                .filter(mob -> mob.getMobType().equals(mobType))
                .toList();

        if (mobs.isEmpty()) return;

        Mob selectedMob = selectRandomMob(mobs);
        if (!shouldSpawn(selectedMob)) return;

        sendMobMessage(messageChannelUnion, selectedMob);
    }

    /**
     * This function wraps the functionality of generating and sending the message
     * corresponding to a mob spawn
     *
     * @param messageChannelUnion The channel the message should be sent to
     * @param mob                 used to generate the message
     */
    private void sendMobMessage(MessageChannelUnion messageChannelUnion, Mob mob) {
        try {
            EmbedBuilder embedBuilder = createMobEmbedded(mob);
            messageChannelUnion.sendMessageEmbeds(embedBuilder.build())
                    .addFiles(FileUpload.fromData(mob.getImgFile(), "mob.png"))
                    .addActionRow(
                            Button.danger(ButtonManager.HIT_BUTTON_ID + mob.getID(), "Hit")
                    )
                    .queue();
            Main.LOGGER.info("Mob spawned: " + mob.getName());
        } catch (NullPointerException ex) {
            Main.LOGGER.error("Could not load image file: " + ex.getMessage());
        }
    }

    /**
     * Creates an embedded based on the give mob
     *
     * @param mob The mob in question
     * @return An embedded
     */

    private EmbedBuilder createMobEmbedded(Mob mob) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(mob.getName());
        embedBuilder.setColor(Color.RED);
        embedBuilder.setDescription(mob.getDescription());
        embedBuilder.setImage("attachment://mob.png");

        return embedBuilder;
    }

    /**
     * Selects one random mob from the give list
     *
     * @param mobs A list of mobs
     * @return One single mob
     */
    private Mob selectRandomMob(List<Mob> mobs) {
        int selector = RANDOM.nextInt(mobs.size());
        return mobs.get(selector);
    }

    /**
     * Actual probability check if a mob should spawn
     *
     * @param mob The mob in question
     * @return True or false, depending on the check
     */
    private boolean shouldSpawn(Mob mob) {
        return RANDOM.nextDouble() <= mob.getSpawnChance();
    }
}
