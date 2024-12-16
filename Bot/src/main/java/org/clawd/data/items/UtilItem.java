package org.clawd.data.items;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.clawd.data.inventory.UserStats;
import org.clawd.data.items.enums.ItemType;
import org.clawd.tokens.Constants;

public class UtilItem extends Item {
    private final double goldMultiplier;

    public UtilItem(
            int uniqueID,
            String name,
            String desc,
            String itemEmoji,
            String imgPath,
            int reqLvl,
            ItemType itemType,
            double dropChance,
            double xpMultiplier,
            double goldMultiplier
    ) {
        super(uniqueID, name, desc, itemEmoji, imgPath, reqLvl, itemType, dropChance, xpMultiplier);
        this.goldMultiplier = goldMultiplier;
        this.price = calculatePrice(xpMultiplier, goldMultiplier);
    }

    @Override
    public Field createShopField() {
        return new Field(
                this.getEmoji() + this.getName() + this.getEmoji(),
                Constants.BLACK_SMALL_SQUARE + " XP boost: " + this.getXpMultiplier() + "\n"
                        + Constants.BLACK_SMALL_SQUARE + " Gold boost: " + this.getGoldMultiplier() + "\n"
                        + Constants.BLACK_SMALL_SQUARE + " Price: " + this.getPrice() + " Coins" + "\n"
                        + Constants.BLACK_SMALL_SQUARE + " lvl. " + this.getReqLvl(),
                true
        );
    }

    @Override
    public EmbedBuilder createInspectEmbed(UserStats userStats, Button buyButton, Button equipButton) {

        String priceEmoji = buyButton.isDisabled() ? Constants.RED_CROSS : Constants.BLACK_SMALL_SQUARE;
        String lvlEmoji = buyButton.isDisabled() ? Constants.RED_CROSS : Constants.BLACK_SMALL_SQUARE;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(":mag: " + this.getName() + " :mag:")
                .setThumbnail("attachment://item.png")
                .addField(
                        this.getDescription(),
                        Constants.BLACK_SMALL_SQUARE + " XP boost: " + this.getXpMultiplier() + "\n"
                                + Constants.BLACK_SMALL_SQUARE + " Gold boost: " + this.getGoldMultiplier() + "\n"
                                + priceEmoji + " Price: " + this.getPrice() + " Coins" + "\n"
                                + lvlEmoji + " Required lvl. " + this.getReqLvl(),
                        false
                );

        return embedBuilder;
    }

    public double getGoldMultiplier() {
        return goldMultiplier;
    }
}
