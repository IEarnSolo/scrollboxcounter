package com.scrollboxcounter;

import com.google.inject.Provides;
import java.util.Objects;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.game.ItemManager;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.GameTick;

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

	public static final int CLUE_SCROLL_BEGINNER = 23182;
	public static final int CLUE_SCROLL_MASTER = 19835;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ScrollBoxCounterOverlay overlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Getter
    @Inject
	private ItemManager itemManager;

	@Inject
	private Client client;

	private final Map<Integer, Integer> bankItems = new HashMap<>();
	private final Map<Integer, Integer> bankActiveClueScrolls = new HashMap<>();
	private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
	private boolean bankJustClosed = false;
	private boolean bankVisited = false;
	private boolean suppressChatMessage = false;
	private boolean usePendingBankItems = false;
	private boolean suppressChatOnStartup = true;
	private boolean wasBankOpenLastTick = false;

	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		suppressChatOnStartup = true;
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		bankItems.clear();
		bankActiveClueScrolls.clear();
		previousInventoryCounts.clear();
		bankVisited = false;
		bankJustClosed = false;
	}

	@Provides
	ScrollBoxCounterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ScrollBoxCounterConfig.class);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.BANK) {
			updateBankItems(event.getItemContainer());
			bankVisited = true;
		}
		if (event.getContainerId() == InventoryID.INV) {
			handleInventoryChange(event.getItemContainer());
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event) {
		if (event.getGroupId() == InterfaceID.BANKMAIN) {
			bankJustClosed = true;
			suppressChatMessage = true;
			usePendingBankItems = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (bankJustClosed) {
			updateBankItems(client.getItemContainer(InventoryID.BANK));
			bankJustClosed = false;
		}
		usePendingBankItems = false;
		suppressChatMessage = false;
		suppressChatOnStartup = false;
		wasBankOpenLastTick = ScrollBoxCounterUtils.isBankOpen(client);
	}

	private void updateBankItems(ItemContainer bank) {
		if (bank == null) {
			return;
		}
		bankItems.clear();
		bankActiveClueScrolls.clear();

		Item[] items = bank.getItems();
		for (Item item : items) {
			if (item != null && item.getId() != -1 && item.getQuantity() > 0) {
				if (ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
					bankItems.put(item.getId(), item.getQuantity());
				}
				if (isAnyActiveClueScroll(item.getId())) {
					bankActiveClueScrolls.put(item.getId(), item.getQuantity());
				}
			}
		}
	}

	private boolean isAnyActiveClueScroll(int itemId) {
		String[] tiers = {"Beginner", "Easy", "Medium", "Hard", "Elite", "Master"};
		for (String tier : tiers) {
			if (ScrollBoxCounterUtils.isActiveClueScroll(itemId, tier, itemManager)) {
				return true;
			}
		}
		return false;
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

    public int getBankActiveClueScrollCount(int scrollBoxItemId) {
        String tier = ScrollBoxCounterUtils.getScrollBoxTierName(scrollBoxItemId);

        for (Map.Entry<Integer, Integer> entry : bankActiveClueScrolls.entrySet()) {
            int itemId = entry.getKey();
            if (ScrollBoxCounterUtils.isActiveClueScroll(itemId, tier, itemManager)) {
                return 1;
            }
        }

        return 0;
    }

	private void handleInventoryChange(ItemContainer inventory) {
		if (inventory == null) {
			return;
		}
		Map<Integer, Integer> currentCounts = new HashMap<>();
		for (Item item : inventory.getItems()) {
			if (item != null && item.getId() != -1 && ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
				currentCounts.put(item.getId(), item.getQuantity());
			}
		}

		if (!ScrollBoxCounterUtils.isBankOpen(client) && bankVisited) {
			for (Map.Entry<Integer, Integer> entry : currentCounts.entrySet()) {
				int itemId = entry.getKey();
				int currentCount = entry.getValue();
				int previousCount = previousInventoryCounts.getOrDefault(itemId, 0);

				if (currentCount > previousCount && wasBankOpenLastTick) {
					int withdrawn = currentCount - previousCount;
					int oldBank = bankItems.getOrDefault(itemId, 0);
					int newBank = Math.max(0, oldBank - withdrawn);
					bankItems.put(itemId, newBank);
				}
			}
		}
		if (!suppressChatOnStartup && !suppressChatMessage && !ScrollBoxCounterUtils.isBankOpen(client) && Objects.requireNonNull(getConfig()).showChatMessages()) {
			for (Map.Entry<Integer, Integer> entry : currentCounts.entrySet()) {
				int itemId = entry.getKey();
				int currentCount = entry.getValue();
				int previousCount = previousInventoryCounts.getOrDefault(itemId, 0);
				if (currentCount > previousCount) {
					sendScrollBoxMessage(itemId, currentCount);
				}
			}
		}
		previousInventoryCounts.clear();
		previousInventoryCounts.putAll(currentCounts);
	}


	private void sendScrollBoxMessage(int itemId, int inventoryCount) {
		String tierName = ScrollBoxCounterUtils.getScrollBoxTierName(itemId);
		int bankCount = getOverlayBankCount(itemId);
		int totalCount = inventoryCount + bankCount;
		int maxCount = ScrollBoxCounterUtils.getMaxClueCount(itemId, client);
		String message = "Holding " + totalCount + "/" + maxCount + " scroll boxes / clues (" + tierName + ")";
		sendChatMessage(message);
	}

	private ScrollBoxCounterConfig getConfig() {
		return overlay != null ? overlay.getConfig() : null;
	}

	public boolean hasVisitedBank() {
		return bankVisited;
	}


	public int getOverlayBankCount(int itemId) {
		return bankItems.getOrDefault(itemId, 0);
	}

	public boolean isUsingPendingBankItems() {
		return usePendingBankItems;
	}
}
