package com.scrollboxcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("scrollboxcounter")
public interface ScrollBoxCounterConfig extends Config
{
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
		description = "Choose where to display the maximum clue scroll count"
	)
	default MaxCluePosition maxCluePosition()
	{
		return MaxCluePosition.BOTTOM_LEFT;
	}

	@ConfigItem(
		keyName = "markFullStacks",
		name = "Mark Full Stacks",
		description = "Mark the counter red when the item count equals the maximum"
	)
	default boolean markFullStacks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBanked",
		name = "Show Banked",
		description = "Show the quantity of clue scroll boxes in the bank when viewing inventory"
	)
	default boolean showBanked()
	{
		return false;
	}
}
