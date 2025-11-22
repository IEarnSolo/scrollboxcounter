package com.scrollboxinfo.data;

import com.scrollboxinfo.ClueTier;
import com.scrollboxinfo.ScrollBoxInfoConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumMap;

@Singleton
public class ClueCountStorage
{
    private final EnumMap<ClueTier, Integer> clueCounts = new EnumMap<>(ClueTier.class);
    private final EnumMap<ClueTier, Integer> bankCounts = new EnumMap<>(ClueTier.class);

    private final ScrollBoxInfoConfig config;

    @Inject
    public ClueCountStorage(ScrollBoxInfoConfig config)
    {
        this.config = config;
    }

    public void setCount(ClueTier tier, int count)
    {
        clueCounts.put(tier, count);
    }

    public int getCount(ClueTier tier)
    {
        return clueCounts.getOrDefault(tier, 0);
    }

    public void setBankCount(ClueTier tier, int count)
    {
        bankCounts.put(tier, count);
    }

    public int getBankCount(ClueTier tier)
    {
        return bankCounts.getOrDefault(tier, 0);
    }

}
