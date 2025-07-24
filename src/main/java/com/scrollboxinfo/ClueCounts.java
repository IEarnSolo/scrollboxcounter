package com.scrollboxinfo;

public class ClueCounts {

    private final int scrollBoxCount;
    private final boolean hasClueScroll;
    private final boolean hasChallengeScroll;

    public ClueCounts(int scrollBoxCount, boolean hasClueScroll, boolean hasChallengeScroll) {
        this.scrollBoxCount = scrollBoxCount;
        this.hasClueScroll = hasClueScroll;
        this.hasChallengeScroll = hasChallengeScroll;
    }

    public int scrollBoxCount() {
        return scrollBoxCount;
    }

    public boolean hasClueScroll() {
        return hasClueScroll;
    }

    public boolean hasChallengeScroll() {
        return hasChallengeScroll;
    }
}