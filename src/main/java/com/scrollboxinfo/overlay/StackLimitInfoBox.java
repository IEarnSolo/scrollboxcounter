package com.scrollboxinfo.overlay;

import com.scrollboxinfo.ClueTier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class StackLimitInfoBox extends InfoBox {
    private final ClueTier tier;
    private final int clueCount;

    public StackLimitInfoBox(BufferedImage image, Plugin plugin, ClueTier tier, int clueCount) {
        super(image, plugin);
        this.tier = tier;
        this.clueCount = clueCount;
    }

    @Override
    public String getText() {
        //return String.valueOf(clueCount);
        return "Full";
    }

    @Override
    public Color getTextColor() {
        return Color.RED;
    }

    @Override
    public String getTooltip() {
        return "Your " + tier.name().toLowerCase() + " clue stack is full.";
    }
}