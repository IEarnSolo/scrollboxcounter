package com.scrollboxcounter;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Scroll Box Counter",
	description = "Displays the maximum number of Clue Scroll Boxes you can hold per tier.",
	tags = {"clue","scroll"}
)
public class ScrollBoxCounterPlugin extends Plugin {
	// Clue Scroll Box Item IDs
	public static final int CLUE_SCROLL_BOX_BEGINNER = 24361;
	public static final int CLUE_SCROLL_BOX_EASY = 24362;
	public static final int CLUE_SCROLL_BOX_MEDIUM = 24363;
	public static final int CLUE_SCROLL_BOX_HARD = 24364;
	public static final int CLUE_SCROLL_BOX_ELITE = 24365;
	public static final int CLUE_SCROLL_BOX_MASTER = 24366;

	// Scroll Case Upgrade Varbits
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

	private final Map<Integer, Integer> bankItems = new HashMap<>();

	@Override
	protected void startUp() {
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		bankItems.clear();
	}

	@Provides
	ScrollBoxCounterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ScrollBoxCounterConfig.class);
	}

	/**
	 * Checks if the given item ID is a clue scroll box.
	 */
	public static boolean isClueScrollBox(int itemId) {
		return itemId == CLUE_SCROLL_BOX_BEGINNER ||
				itemId == CLUE_SCROLL_BOX_EASY ||
				itemId == CLUE_SCROLL_BOX_MEDIUM ||
				itemId == CLUE_SCROLL_BOX_HARD ||
				itemId == CLUE_SCROLL_BOX_ELITE ||
				itemId == CLUE_SCROLL_BOX_MASTER;
	}

	/**
	 * Handles bank container changes to track clue scroll box quantities.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.BANK) {
			updateBankItems(event.getItemContainer());
		}
	}

	/**
	 * Updates the cached bank items for clue scroll boxes.
	 */
	private void updateBankItems(ItemContainer bank) {
		bankItems.entrySet().removeIf(entry -> isClueScrollBox(entry.getKey()));

		if (bank != null) {
			Item[] items = bank.getItems();
			for (Item item : items) {
				if (item != null && item.getId() != -1 && item.getQuantity() > 0 && isClueScrollBox(item.getId())) {
						bankItems.put(item.getId(), item.getQuantity());
					}

			}
		}
	}

	/**
	 * Gets the count of a specific clue scroll box in the bank.
	 */
	public int getBankCount(int itemId) {
		return bankItems.getOrDefault(itemId, 0);
	}
}
