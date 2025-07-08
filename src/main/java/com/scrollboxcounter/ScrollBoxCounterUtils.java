package com.scrollboxcounter;

import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.ItemContainer;

public class ScrollBoxCounterUtils {

    private static final int BASE_CLUE_COUNT = 2;

    public static boolean isClueScrollBox(int itemId) {
        return itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_BEGINNER ||
                itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_EASY ||
                itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MEDIUM ||
                itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_HARD ||
                itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_ELITE ||
                itemId == ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MASTER;
    }

    public static String getScrollBoxTierName(int itemId) {
        switch (itemId) {
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_BEGINNER:
                return "Beginner";
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_EASY:
                return "Easy";
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MEDIUM:
                return "Medium";
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_HARD:
                return "Hard";
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_ELITE:
                return "Elite";
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MASTER:
                return "Master";
            default:
                return "Unknown";
        }
    }

    public static int getMaxClueCount(int itemId, Client client) {
        int tierBonus = getTierBonus(itemId, client);
        int mimicBonus = getMimicBonus(client);
        return BASE_CLUE_COUNT + tierBonus + mimicBonus;
    }

    public static int getTierBonus(int itemId, Client client) {
        int bonus = 0;

        switch (itemId) {
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_BEGINNER:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_BEGINNER_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_BEGINNER_MAJOR);
                break;
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_EASY:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_EASY_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_EASY_MAJOR);
                break;
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MEDIUM:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MEDIUM_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MEDIUM_MAJOR);
                break;
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_HARD:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_HARD_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_HARD_MAJOR);
                break;
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_ELITE:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_ELITE_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_ELITE_MAJOR);
                break;
            case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MASTER:
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MASTER_MINOR);
                bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MASTER_MAJOR);
                break;
        }

        return bonus;
    }

    public static int getMimicBonus(Client client) {
        return client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MIMIC);
    }

    public static int getInventoryCount(int itemId, Client client) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INV);
        if (inventory == null) {
            return 0;
        }
        return inventory.count(itemId);
    }

    public static boolean isBankOpen(Client client) {
        return client.getItemContainer(InventoryID.BANK) != null;
    }
}