package com.scrollboxcounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ScrollBoxCounterTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ScrollBoxCounterPlugin.class);
		RuneLite.main(args);
	}
}