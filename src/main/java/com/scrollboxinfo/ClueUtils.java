package com.scrollboxinfo;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;

import javax.inject.Inject;

public class ClueUtils
{
    @Inject
    private ScrollBoxInfoConfig config;

    public static ClueTier getClueTier(Client client, int itemId)
    {
        switch (itemId)
        {
            case ItemID.SCROLL_BOX_BEGINNER:
            case ItemID.CLUE_SCROLL_BEGINNER:
                return ClueTier.BEGINNER;

            case ItemID.SCROLL_BOX_EASY:
            case ItemID.CLUE_SCROLL_EASY:
                return ClueTier.EASY;

            case ItemID.SCROLL_BOX_MEDIUM:
            case ItemID.CLUE_SCROLL_MEDIUM:
                return ClueTier.MEDIUM;

            case ItemID.SCROLL_BOX_HARD:
            case ItemID.CLUE_SCROLL_HARD:
                return ClueTier.HARD;

            case ItemID.SCROLL_BOX_ELITE:
            case ItemID.CLUE_SCROLL_ELITE:
                return ClueTier.ELITE;

            case ItemID.SCROLL_BOX_MASTER:
            case ItemID.CLUE_SCROLL_MASTER:
                return ClueTier.MASTER;

            default:
                ItemComposition item = client.getItemDefinition(itemId);
                if (item == null)
                {
                    return null;
                }

                String name = item.getName().toLowerCase();

                if (name.startsWith("clue scroll (beginner)"))
                {
                    return ClueTier.BEGINNER;
                }
                else if (name.startsWith("clue scroll (easy)"))
                {
                    return ClueTier.EASY;
                }
                else if (name.startsWith("clue scroll (medium)"))
                {
                    return ClueTier.MEDIUM;
                }
                else if (name.startsWith("clue scroll (hard)"))
                {
                    return ClueTier.HARD;
                }
                else if (name.startsWith("clue scroll (elite)"))
                {
                    return ClueTier.ELITE;
                }
                else if (name.startsWith("clue scroll (master)"))
                {
                    return ClueTier.MASTER;
                }
                else if (name.startsWith("challenge scroll (medium)"))
                {
                    return ClueTier.MEDIUM;
                }
                else if (name.startsWith("challenge scroll (hard)"))
                {
                    return ClueTier.HARD;
                }
                else if (name.startsWith("challenge scroll (elite)"))
                {
                    return ClueTier.ELITE;
                }
                else
                {
                    return null;
                }
        }
    }

    public int getClueItemId(ClueTier tier) {
        switch (tier) {
            case BEGINNER:
                return ItemID.SCROLL_BOX_BEGINNER;
            case EASY:
                return ItemID.SCROLL_BOX_EASY;
            case MEDIUM:
                return ItemID.SCROLL_BOX_MEDIUM;
            case HARD:
                return ItemID.SCROLL_BOX_HARD;
            case ELITE:
                return ItemID.SCROLL_BOX_ELITE;
            case MASTER:
                return ItemID.SCROLL_BOX_MASTER;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public boolean isClueOrChallengeScroll(Client client, int itemId)
    {
        if (itemId <= 0)
        {
            return false;
        }

        ItemComposition item = client.getItemDefinition(itemId);
        if (item == null)
        {
            return false;
        }

        String name = item.getName().toLowerCase();
        if (name == null)
        {
            return false;
        }

        if (name.startsWith("clue scroll (") || name.startsWith("challenge scroll ("))
        {
            return true;
        }
        return false;
    }

    public static String formatTierName(ClueTier tier) {
        String name = tier.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String getFormattedTierName(ClueTier tier)
    {
        String tierName = ClueUtils.formatTierName(tier);

        if (tier == ClueTier.BEGINNER)
        {
            tierName = tierName.substring(0, tierName.length() - 2);
        }

        if (config.useTierShortnames())
        {
            switch (tier)
            {
                case BEGINNER:
                    return "Beg";
                case MEDIUM:
                    return "Med";
                case MASTER:
                    return "Mstr";
                default:
                    return tierName;
            }
        }

        return tierName;
    }

}