package com.scrollboxinfo;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.scrollboxinfo.data.ClueCountStorage;
import com.scrollboxinfo.overlay.ClueWidgetItemOverlay;
import com.scrollboxinfo.overlay.StackLimitInfoBox;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.InventoryID;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "Scroll Box Info",
	description = "Keep track of how many clues you have, your current clue stack limit, and how many clues until next stack limit increase",
	tags = {"scroll", "watson", "case"}
)
public class ScrollBoxInfoPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ScrollBoxInfoConfig config;
	@Inject
	private QuestChecker questChecker;
	@Inject
	private ClueCountStorage clueCountStorage;
	@Inject
	private ClueCounter clueCounter;
	@Inject
	private ClueWidgetItemOverlay clueWidgetItemOverlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private ClueUtils clueUtils;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	private StackLimitInfoBox stackInfoBox;
	@Inject
	private ClientThread clientThread;

	private boolean bankWasOpenLastTick = false;
	private boolean bankIsOpen = false;
	private boolean depositBoxIsOpen = false;
	private boolean depositBoxWasOpenLastTick = false;
	private final Map<ClueTier, Integer> previousInventoryScrollBoxCount = new HashMap<>();
	private final Map<ClueTier, Boolean> previousInventoryClueScrollState = new HashMap<>();
	private final Map<ClueTier, Boolean> previousInventoryChallengeScrollState = new HashMap<>();
	private final Map<ClueTier, Integer> previousBankScrollBoxCount = new HashMap<>();
	private final Map<ClueTier, Boolean> previousBankClueScrollState = new HashMap<>();
	private final Map<ClueTier, Boolean> previousBankChallengeScrollState = new HashMap<>();
	private final Map<ClueTier, Integer> previousTotalClueCounts = new HashMap<>();
	private final Map<ClueTier, StackLimitInfoBox> stackInfoBoxes = new HashMap<>();

	private void checkAndDisplayInfobox(ClueTier tier, int count, int cap) {
		if (!config.showFullStackInfobox() || !isTierInfoboxEnabled(tier)) {
			StackLimitInfoBox box = stackInfoBoxes.remove(tier);
			if (box != null) {
				infoBoxManager.removeInfoBox(box);
			}
			return;
		}

		StackLimitInfoBox box = stackInfoBoxes.get(tier);

		if (count >= cap) {
			if (box == null) {
				int clueItemId = clueUtils.getClueItemId(tier);
				BufferedImage image = itemManager.getImage(clueItemId);
				box = new StackLimitInfoBox(image, this, tier, count);
				infoBoxManager.addInfoBox(box);
				stackInfoBoxes.put(tier, box);
			}
		} else if (box != null) {
			infoBoxManager.removeInfoBox(box);
			stackInfoBoxes.remove(tier);
		}
	}

	private boolean isTierInfoboxEnabled(ClueTier tier) {
		switch (tier) {
			case BEGINNER:
				return config.showBeginnerInfobox();
			case EASY:
				return config.showEasyInfobox();
			case MEDIUM:
				return config.showMediumInfobox();
			case HARD:
				return config.showHardInfobox();
			case ELITE:
				return config.showEliteInfobox();
			case MASTER:
				return config.showMasterInfobox();
			default:
				return true;
		}
	}

	private void sendTotalClueCountsChatMessage()
	{
		for (ClueTier tier : ClueTier.values())
		{
			int current = clueCounter.getClueCounts(tier);
			int cap = StackLimitCalculator.getStackLimit(tier, client);

			String color = (current == cap) ? "ff0000" : "006600"; // red : green
			String message = String.format(
					"<col=%s>%s clue count: %d/%d",
					color,
					ClueUtils.formatTierName(tier),
					current,
					cap
			);

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		clueCountStorage.loadBankCountsFromConfig();
		overlayManager.add(clueWidgetItemOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(clueWidgetItemOverlay);
		clueWidgetItemOverlay.resetMarkedStacks();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("scrollboxinfo"))
			return;

		if (event.getKey().equals("markFullStack") && !config.markFullStack())
		{
			clueWidgetItemOverlay.resetMarkedStacks();
		}

		if (event.getKey().equals("showFullStackInfobox")
				|| event.getKey().equals("showBeginnerInfobox")
				|| event.getKey().equals("showEasyInfobox")
				|| event.getKey().equals("showMediumInfobox")
				|| event.getKey().equals("showHardInfobox")
				|| event.getKey().equals("showEliteInfobox")
				|| event.getKey().equals("showMasterInfobox"))
		{
			clientThread.invokeLater(() ->
			{
				for (ClueTier tier : ClueTier.values())
				{
					int count = clueCounter.getClueCounts(tier);
					int cap = StackLimitCalculator.getStackLimit(tier, client);
					checkAndDisplayInfobox(tier, count, cap);
				}
			});
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		bankWasOpenLastTick = bankIsOpen;
		depositBoxWasOpenLastTick = depositBoxIsOpen;

		Widget bankWidget = client.getWidget(ComponentID.BANK_CONTAINER);
		bankIsOpen = bankWidget != null && !bankWidget.isHidden();

		Widget depositBoxWidget = client.getWidget(ComponentID.DEPOSIT_BOX_INVENTORY_ITEM_CONTAINER);
		depositBoxIsOpen = depositBoxWidget != null && !depositBoxWidget.isHidden();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);

		for (ClueTier tier : ClueTier.values())
		{
			ClueCounts inventory = clueCounter.getClueCounts(tier, inventoryContainer);
			ClueCounts bank = clueCounter.getClueCounts(tier, bankContainer);

			boolean clueEnteredInv = inventory.hasClueScroll() && !previousInventoryClueScrollState.getOrDefault(tier, false);
			boolean clueLeftInv = !inventory.hasClueScroll() && previousInventoryClueScrollState.getOrDefault(tier, false);
			boolean challengeEnteredInv = inventory.hasChallengeScroll() && !previousInventoryChallengeScrollState.getOrDefault(tier, false);
			boolean challengeLeftInv = !inventory.hasChallengeScroll() && previousInventoryChallengeScrollState.getOrDefault(tier, false);
			boolean scrollBoxEnteredInv = inventory.scrollBoxCount() > previousInventoryScrollBoxCount.getOrDefault(tier, 0);
			boolean scrollBoxLeftInv = inventory.scrollBoxCount() < previousInventoryScrollBoxCount.getOrDefault(tier, 0);

			boolean bankedClueScroll = previousBankClueScrollState.getOrDefault(tier, false);
			boolean bankedChallengeScroll = previousBankChallengeScrollState.getOrDefault(tier, false);
			int assumedBankedScrollBoxCount = previousBankScrollBoxCount.getOrDefault(tier, 0);
			boolean hasScrollInBank = Boolean.TRUE.equals(
					configManager.getRSProfileConfiguration("scrollboxinfo", "hasClueOrChallengeScrollInBank_" + tier.name(), Boolean.class)
			);


			int count = inventory.scrollBoxCount();
			if (inventory.hasClueScroll())
				count++;
			if (inventory.hasChallengeScroll())
				count++;

			if (bankContainer != null) {
				bankedClueScroll = bank.hasClueScroll();
				bankedChallengeScroll = bank.hasChallengeScroll();
				assumedBankedScrollBoxCount = bank.scrollBoxCount();

				int bankCount = assumedBankedScrollBoxCount;
				bankCount += bankedClueScroll ? 1 : 0;
				bankCount += bankedChallengeScroll ? 1 : 0;

				if (bankedChallengeScroll && bankedClueScroll)
					bankCount -= 1;

				if (bankedChallengeScroll || bankedClueScroll)
				{
					configManager.setRSProfileConfiguration("scrollboxinfo", "hasClueOrChallengeScrollInBank_" + tier.name(), true);
				}
				if (!bankedChallengeScroll && !bankedClueScroll)
				{
					configManager.setRSProfileConfiguration("scrollboxinfo", "hasClueOrChallengeScrollInBank_" + tier.name(), false);
				}

				clueCountStorage.setBankCount(tier, bankCount);
			} else if (bankWasOpenLastTick || depositBoxIsOpen || depositBoxWasOpenLastTick) {
				if (scrollBoxLeftInv)
					assumedBankedScrollBoxCount += 1;
				else if (scrollBoxEnteredInv)
					assumedBankedScrollBoxCount -= 1;

				if (clueEnteredInv) {
					bankedClueScroll = false;
				} else if (clueLeftInv) {
					bankedClueScroll = true;
				}
				if (challengeEnteredInv) {
					bankedChallengeScroll = false;
				} else if (challengeLeftInv) {
					bankedChallengeScroll = true;
				}

				if (bankedChallengeScroll || bankedClueScroll)
				{
					configManager.setRSProfileConfiguration("scrollboxinfo", "hasClueOrChallengeScrollInBank_" + tier.name(), true);
				}
				if (!bankedChallengeScroll && !bankedClueScroll)
				{
					configManager.setRSProfileConfiguration("scrollboxinfo", "hasClueOrChallengeScrollInBank_" + tier.name(), false);
				}

				int assumedBankCount = assumedBankedScrollBoxCount;
				if (bankedChallengeScroll || bankedClueScroll)
					assumedBankCount += 1;

				clueCountStorage.setBankCount(tier, assumedBankCount);
			}

			count += clueCountStorage.getBankCount(tier);
			if ((inventory.hasClueScroll() && inventory.hasChallengeScroll())
				|| (inventory.hasClueScroll() && bankedChallengeScroll)
				|| (inventory.hasChallengeScroll() && bankedClueScroll)
				|| ((inventory.hasClueScroll() || inventory.hasChallengeScroll()) && hasScrollInBank))
				count -= 1;
			clueCountStorage.setCount(tier, count);

			int cap = StackLimitCalculator.getStackLimit(tier, client);

			if (config.showFullStackInfobox())
				checkAndDisplayInfobox(tier, count, cap);

			int previousTotalClueCount = previousTotalClueCounts.getOrDefault(tier, 0);
			if (config.showChatMessage()) {
				if (scrollBoxEnteredInv
						&& (clueCounter.getClueCounts(tier) != previousTotalClueCount)
						&& previousTotalClueCounts.containsKey(tier)
						&& bankContainer == null
						&& !bankWasOpenLastTick
						&& !depositBoxIsOpen
						&& !depositBoxWasOpenLastTick) {

					String color = (count == cap) ? "ff0000" : "006600"; // red : green
					String message = String.format("<col=%s>Current %s clue count: %d/%d", color, tier.name().toLowerCase(), count, cap);
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
				}
			}

			previousBankScrollBoxCount.put(tier, assumedBankedScrollBoxCount);
			previousBankClueScrollState.put(tier, bankedClueScroll);
			previousBankChallengeScrollState.put(tier, bankedChallengeScroll);
			previousInventoryScrollBoxCount.put(tier, inventory.scrollBoxCount());
			previousInventoryClueScrollState.put(tier, inventory.hasClueScroll());
			previousInventoryChallengeScrollState.put(tier, inventory.hasChallengeScroll());
			previousTotalClueCounts.put(tier, clueCountStorage.getCount(tier));
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.showInventoryRightClickOption()) return;

		if (event.getOption().equals("Inventory"))
		{
			client.getMenu().createMenuEntry(1)
					.setOption("View clue counts")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> sendTotalClueCountsChatMessage())
					.setDeprioritized(true);
		}
	}


	@Provides
	ScrollBoxInfoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScrollBoxInfoConfig.class);
	}
}