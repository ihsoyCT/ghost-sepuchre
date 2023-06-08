package com.ihsoy.ghostsepulchre;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GhostSepulchreTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GhostSepulchrePlugin.class);
		RuneLite.main(args);
	}
}