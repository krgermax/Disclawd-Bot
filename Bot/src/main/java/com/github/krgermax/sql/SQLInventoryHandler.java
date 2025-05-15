package com.github.krgermax.sql;

import com.github.krgermax.data.items.Item;
import com.github.krgermax.main.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLInventoryHandler {

    public void addItemToPlayer(String userID, int itemID) {
        try {
            Connection connection = Main.bot.getSQLConnection();
            String sqlQuery = "INSERT INTO inventory ("
                    + SQLManager.USER_ID_COLUMN_LABEL + ", "
                    + SQLManager.ITEM_ID_COLUMN_LABEL + ") "
                    + "VALUES (?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setString(1, userID);
            preparedStatement.setInt(2, itemID);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Main.LOGGER.error("Some SQL error occurred: " + ex.getMessage());
        }
    }

    public boolean isItemInUserInventory(String userID, int itemID) {
        boolean itemInInventory = false;
        try {
            Connection connection = Main.bot.getSQLConnection();
            String sqlQuery = "SELECT * FROM inventory WHERE "
                    + SQLManager.USER_ID_COLUMN_LABEL + " = ? AND "
                    + SQLManager.ITEM_ID_COLUMN_LABEL + " = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setString(1, userID);
            preparedStatement.setInt(2, itemID);
            ResultSet resultSet = preparedStatement.executeQuery();

            itemInInventory = resultSet.next();
            resultSet.close();
        } catch (SQLException ex) {
            Main.LOGGER.error("Some SQL error occurred: " + ex.getMessage());
        }
        return itemInInventory;
    }

    /**
     * This method equips an item for a given user ID.
     *
     * @param userID The user ID
     * @param itemID The item ID to equip
     */
    public void equipItem(String userID, int itemID) {
        try {
            Main.LOGGER.info("Equipping item " + itemID + " for user " + userID);
            Connection connection = Main.bot.getSQLConnection();
            String sqlQuery = "UPDATE players SET "
                    + SQLManager.EQUIPPED_ITEM_COLUMN_LABEL + " = ? WHERE "
                    + SQLManager.USER_ID_COLUMN_LABEL + " = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, itemID);
            preparedStatement.setString(2, userID);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                Main.LOGGER.info("Equipped item " + itemID + " for user " + userID);
            } else {
                Main.LOGGER.error("Failed to equip item " + itemID + " for user " + userID);
            }
            preparedStatement.close();

        } catch (SQLException ex) {
            Main.LOGGER.error("Some SQL error occurred: " + ex.getMessage());
        }
    }

    /**
     * This method returns the equipped item ID, if no item is equipped this method returns -1
     *
     * @param userID The user ID
     * @return Either an item ID or -1
     */
    public int getEquippedItemIDFromUser(String userID) {
        int itemID = -1;
        try {
            Connection connection = Main.bot.getSQLConnection();
            String sqlQuery = "SELECT "
                    + SQLManager.EQUIPPED_ITEM_COLUMN_LABEL + " FROM players WHERE "
                    + SQLManager.USER_ID_COLUMN_LABEL + " = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                itemID = resultSet.getInt(SQLManager.EQUIPPED_ITEM_COLUMN_LABEL);
                Main.LOGGER.info("Retrieved item ID: " + itemID + ". From user: " + userID);
            }

            resultSet.close();
            preparedStatement.close();

        } catch (SQLException ex) {
            Main.LOGGER.error("Some SQL error occurred: " + ex.getMessage());
        }
        return itemID;
    }

    /**
     * This method returns a list of all items that the user has bought or found
     *
     * @param userID The user ID
     * @return A list containing the items or an empty list
     */
    public List<Item> getAllUserItems(String userID) {
        List<Item> itemList = new ArrayList<>();

        try {
            Connection connection = Main.bot.getSQLConnection();
            String sqlQuery = "SELECT * FROM inventory WHERE "
                    + SQLManager.USER_ID_COLUMN_LABEL + " = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int itemID = resultSet.getInt(SQLManager.ITEM_ID_COLUMN_LABEL);
                Item item = Main.shopManager.getItemByID(itemID);
                itemList.add(item);
            }

        } catch (SQLException ex) {
            Main.LOGGER.error("Some SQL error occurred: " + ex.getMessage());
        }
        return itemList;
    }
}
