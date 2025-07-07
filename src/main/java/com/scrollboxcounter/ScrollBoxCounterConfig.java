package com.scrollboxcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("scrollboxcounter")
public interface ScrollBoxCounterConfig extends Config
{
	@ConfigItem(
		keyName = "showCounter",
		name = "Show Counter",
		description = "Show the quantity counter on clue scroll boxes"
	)
	default boolean showCounter()
	{
		return true;
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
}
