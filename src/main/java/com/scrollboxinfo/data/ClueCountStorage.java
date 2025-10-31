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

        switch (tier)
        {
            case BEGINNER:
                config.setTotalBeginner(count);
                break;
            case EASY:
                config.setTotalEasy(count);
                break;
            case MEDIUM:
                config.setTotalMedium(count);
                break;
            case HARD:
                config.setTotalHard(count);
                break;
            case ELITE:
                config.setTotalElite(count);
                break;
            case MASTER:
                config.setTotalMaster(count);
                break;
        }
    }

    public int getCount(ClueTier tier)
    {
        return clueCounts.getOrDefault(tier, 0);
    }

    public void setBankCount(ClueTier tier, int count)
    {
        bankCounts.put(tier, count);

        switch (tier)
        {
            case BEGINNER:
                config.setBankedBeginner(count);
                break;
            case EASY:
                config.setBankedEasy(count);
                break;
            case MEDIUM:
                config.setBankedMedium(count);
                break;
            case HARD:
                config.setBankedHard(count);
                break;
            case ELITE:
                config.setBankedElite(count);
                break;
            case MASTER:
                config.setBankedMaster(count);
                break;
        }
    }

    public void loadBankCountsFromConfig()
    {
        bankCounts.put(ClueTier.BEGINNER, config.bankedBeginner());
        bankCounts.put(ClueTier.EASY, config.bankedEasy());
        bankCounts.put(ClueTier.MEDIUM, config.bankedMedium());
        bankCounts.put(ClueTier.HARD, config.bankedHard());
        bankCounts.put(ClueTier.ELITE, config.bankedElite());
        bankCounts.put(ClueTier.MASTER, config.bankedMaster());
    }

    public int getBankCount(ClueTier tier)
    {
        return bankCounts.getOrDefault(tier, 0);
    }

    public void loadTotalCountsFromConfig()
    {
        clueCounts.put(ClueTier.BEGINNER, config.totalBeginner());
        clueCounts.put(ClueTier.EASY, config.totalEasy());
        clueCounts.put(ClueTier.MEDIUM, config.totalMedium());
        clueCounts.put(ClueTier.HARD, config.totalHard());
        clueCounts.put(ClueTier.ELITE, config.totalElite());
        clueCounts.put(ClueTier.MASTER, config.totalMaster());
    }
}
