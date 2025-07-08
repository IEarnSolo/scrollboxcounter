package com.scrollboxcounter;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.TileItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Scroll Box Counter"
)
public class ScrollBoxCounterPlugin extends Plugin {
	public static final int CLUE_SCROLL_BOX_BEGINNER = 24361;
	public static final int CLUE_SCROLL_BOX_EASY = 24362;
	public static final int CLUE_SCROLL_BOX_MEDIUM = 24363;
	public static final int CLUE_SCROLL_BOX_HARD = 24364;
	public static final int CLUE_SCROLL_BOX_ELITE = 24365;
	public static final int CLUE_SCROLL_BOX_MASTER = 24366;

	public static final int SCROLL_CASE_BEGINNER_MINOR = 16565;
	public static final int SCROLL_CASE_BEGINNER_MAJOR = 16566;
	public static final int SCROLL_CASE_EASY_MINOR = 16567;
	public static final int SCROLL_CASE_EASY_MAJOR = 16586;
	public static final int SCROLL_CASE_MEDIUM_MINOR = 16587;
	public static final int SCROLL_CASE_MEDIUM_MAJOR = 16588;
	public static final int SCROLL_CASE_HARD_MINOR = 16589;
	public static final int SCROLL_CASE_HARD_MAJOR = 16590;
	public static final int SCROLL_CASE_ELITE_MINOR = 16591;
	public static final int SCROLL_CASE_ELITE_MAJOR = 16592;
	public static final int SCROLL_CASE_MASTER_MINOR = 16593;
	public static final int SCROLL_CASE_MASTER_MAJOR = 16594;
	public static final int SCROLL_CASE_MIMIC = 16595;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ScrollBoxCounterOverlay overlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	private final Map<Integer, Integer> bankItems = new HashMap<>();
	private final Set<Integer> recentlyPickedUpItems = new HashSet<>();

	@Override
	protected void startUp() {
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		bankItems.clear();
		recentlyPickedUpItems.clear();
	}

	@Provides
	ScrollBoxCounterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ScrollBoxCounterConfig.class);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.BANK) {
			updateBankItems(event.getItemContainer());
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
        if (item != null) {
            item.getId();
        }
    }

	@Subscribe
	public void onItemDespawned(ItemDespawned event) {
		TileItem item = event.getItem();
		if (item != null && ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
			recentlyPickedUpItems.add(item.getId());
		}
	}

	public boolean wasRecentlyPickedUp(int itemId) {
		return recentlyPickedUpItems.remove(itemId);
	}

	private void updateBankItems(ItemContainer bank) {
		bankItems.entrySet().removeIf(entry -> ScrollBoxCounterUtils.isClueScrollBox(entry.getKey()));

		if (bank != null) {
			Item[] items = bank.getItems();
			for (Item item : items) {
				if (item != null && item.getId() != -1 && item.getQuantity() > 0 && ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
						bankItems.put(item.getId(), item.getQuantity());
					}

			}
		}
	}

	public int getBankCount(int itemId) {
		return bankItems.getOrDefault(itemId, 0);
	}

	public void sendChatMessage(String chatMessage)
	{
		final String message = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(chatMessage)
				.build();

		chatMessageManager.queue(
				QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(message)
						.build());
	}
}
