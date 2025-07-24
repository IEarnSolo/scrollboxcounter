package com.scrollboxinfo;

import com.scrollboxinfo.data.ClueCountStorage;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;

@Slf4j
public class ClueCounter
{
    private final Client client;
    private final ClueCountStorage storage;
    private final ItemManager itemManager;

    @Inject
    public ClueCounter(Client client, ClueCountStorage storage, ItemManager itemManager)
    {
        this.client = client;
        this.storage = storage;
        this.itemManager = itemManager;
    }

    public int getClueCounts(ClueTier tier)
    {
        return storage.getCount(tier);
    }

    public ClueCounts getClueCounts(ClueTier tier, ItemContainer container)
    {
        int scrollBoxCount = 0;
        boolean hasClueScroll = false;
        boolean hasChallengeScroll = false;

        if (container != null)
        {
            for (Item item : container.getItems())
            {
                if (item == null || item.getId() <= 0)
                    continue;

                ClueTier itemTier = ClueUtils.getClueTier(client, item.getId());
                if (itemTier != tier)
                    continue;

                ItemComposition itemDef = client.getItemDefinition(item.getId());
                if (itemDef == null)
                    continue;

                String name = itemDef.getName().toLowerCase();

                if (name.startsWith("scroll box ("))
                {
                    scrollBoxCount += item.getQuantity();
                }
                else if (name.startsWith("clue scroll ("))
                {
                    hasClueScroll = true;
                }
                else if (name.startsWith("challenge scroll ("))
                {
                    hasChallengeScroll = true;
                }
            }
        }

        return new ClueCounts(scrollBoxCount, hasClueScroll, hasChallengeScroll);
    }
}