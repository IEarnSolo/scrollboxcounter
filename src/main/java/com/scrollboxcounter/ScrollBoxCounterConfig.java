package com.scrollboxcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("scrollboxcounter")
public interface ScrollBoxCounterConfig extends Config
{
	/**
	 * Position options for displaying the maximum clue scroll count.
	 */
	enum MaxCluePosition
	{
		DISABLED("Disabled"),
		BOTTOM_LEFT("Bottom Left"),
		BOTTOM_RIGHT("Bottom Right");

		private final String name;

		MaxCluePosition(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	@ConfigItem(
		keyName = "maxCluePosition",
		name = "Max Clue Scrolls Position",
		description = "Choose where to display the maximum clue scroll count on each item"
	)
	default MaxCluePosition maxCluePosition()
	{
		return MaxCluePosition.BOTTOM_LEFT;
	}

	@ConfigItem(
		keyName = "markFullStacks",
		name = "Mark Full Stacks",
		description = "Mark counters red when total item count (inventory + bank) reaches maximum capacity"
	)
	default boolean markFullStacks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBanked",
		name = "Show Banked",
		description = "Show the quantity of clue scroll boxes stored in the bank when viewing inventory"
	)
	default boolean showBanked()
	{
		return false;
	}
}
