package com.scrollboxinfo;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ScrollBoxInfoPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ScrollBoxInfoPlugin.class);
		RuneLite.main(args);
	}
}