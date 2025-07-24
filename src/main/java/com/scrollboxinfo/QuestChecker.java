package com.scrollboxinfo;

import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class QuestChecker
{
    private final Client client;

    @Inject
    public QuestChecker(Client client)
    {
        this.client = client;
    }

    public boolean isXMarksTheSpotComplete()
    {
        QuestState state = Quest.X_MARKS_THE_SPOT.getState(client);
        return state == QuestState.FINISHED;
    }
}