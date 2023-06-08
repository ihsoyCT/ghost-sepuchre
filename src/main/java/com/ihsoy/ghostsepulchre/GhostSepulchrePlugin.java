package com.ihsoy.ghostsepulchre;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ihsoy.ghostsepulchre.recording.StateHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class GhostSepulchrePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GhostSepulchreConfig config;

	@Inject
	private StateHandler stateHandler;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged) {
		stateHandler.changeState(varbitChanged);
	}

	@Provides
	GhostSepulchreConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GhostSepulchreConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		stateHandler.run();
	}
}
