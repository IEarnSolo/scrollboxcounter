package com.scrollboxinfo.toa;

import com.scrollboxinfo.ClueCounter;
import com.scrollboxinfo.ClueCounts;
import com.scrollboxinfo.ClueTier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

@Slf4j
@Singleton
public class ToaChestTracker
{
	private static final int BANK_PENDING_TIMEOUT_TICKS = 5;
	// Change this item ID when a different ToA reward is needed to test the Bank-all event flow
	private static final int BANK_TEST_ITEM_ID = ItemID.SAPPHIRE;

	private final Client client;
	private final ClueCounter clueCounter;

	private boolean bankTestMode;
	private int pendingEliteScrollBoxCount;
	private boolean pendingEliteClueScroll;
	private boolean pendingInterfaceClosed;
	private int pendingBankTick = -1;

	@Inject
	public ToaChestTracker(Client client, ClueCounter clueCounter)
	{
		this.client = client;
		this.clueCounter = clueCounter;
	}

	public Optional<ToaBankResult> onGameTick(int tick)
	{
		if (hasPendingReward() && pendingInterfaceClosed && tick > pendingBankTick)
		{
			if (bankTestMode)
			{
				log.debug("[ToA Bank Test] SUCCESS via close fallback: Bank-all was clicked with itemId={}, count={}, and the interface closed before a container confirmation",
						BANK_TEST_ITEM_ID, pendingEliteScrollBoxCount);
				clearPending("applied one-tick ToA interface-close test fallback");
				return Optional.empty();
			}

			ToaBankResult result = new ToaBankResult(
					pendingEliteScrollBoxCount,
					pendingEliteClueScroll,
					"Bank-all click followed by interface close"
			);
			clearPending("applied one-tick ToA interface-close fallback");
			return Optional.of(result);
		}

		if (hasPendingReward() && tick - pendingBankTick > BANK_PENDING_TIMEOUT_TICKS)
		{
			log.debug("Pending ToA Bank-all timed out at tick {} after waiting for {} tick(s): testMode={}, pendingCount={}, pendingClueScroll={}",
					tick, tick - pendingBankTick, bankTestMode, pendingEliteScrollBoxCount, pendingEliteClueScroll);
			clearPending("timed out waiting for the ToA reward container to change");
		}

		return Optional.empty();
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		int componentId = event.getParam1();
		Widget clickedWidget = event.getWidget();
		log.debug("Menu option clicked: option='{}', target='{}', action={}, identifier={}, itemId={}, "
				+ "param0={}, param1/componentId={} (0x{}), interfaceId={}, widgetId={}, widgetIndex={}, widgetParentId={}",
				event.getMenuOption(), event.getMenuTarget(), event.getMenuAction(), event.getId(), event.getItemId(),
				event.getParam0(), componentId, Integer.toHexString(componentId),
				WidgetUtil.componentToInterface(componentId),
				clickedWidget == null ? null : clickedWidget.getId(),
				clickedWidget == null ? null : clickedWidget.getIndex(),
				clickedWidget == null ? null : clickedWidget.getParentId());

		if (WidgetUtil.componentToInterface(componentId) != InterfaceID.TOA_CHESTS)
		{
			return;
		}

		if (componentId != InterfaceID.ToaChests.BANK)
		{
			clearPending("another ToA reward action was clicked: " + event.getMenuOption());
			return;
		}

		log.debug("Recognized ToA Bank-all click: componentId={} (0x{}), action={}, identifier={}, widgetVisible={}",
				componentId, Integer.toHexString(componentId), event.getMenuAction(), event.getId(),
				clickedWidget != null && !clickedWidget.isHidden());

		ItemContainer toaRewards = client.getItemContainer(InventoryID.TOA_CHESTS);
		if (bankTestMode)
		{
			armTestReward(toaRewards);
			return;
		}

		ClueCounts eliteRewards = clueCounter.getClueCounts(ClueTier.ELITE, toaRewards);
		int eliteScrollBoxCount = eliteRewards.scrollBoxCount();
		boolean hasEliteClueScroll = eliteRewards.hasClueScroll();
		log.debug("ToA Bank-all reward snapshot: eliteScrollBoxes={}, eliteClueScroll={}, rewardContainerAvailable={}",
				eliteScrollBoxCount, hasEliteClueScroll, toaRewards != null);
		if (eliteScrollBoxCount <= 0 && !hasEliteClueScroll)
		{
			log.debug("ToA Bank-all click was detected correctly, but no supported elite scroll box or clue scroll was present; no bank detection was armed");
			clearPending("Bank-all was clicked without a supported elite reward in the ToA rewards");
			return;
		}

		pendingEliteScrollBoxCount = eliteScrollBoxCount;
		pendingEliteClueScroll = hasEliteClueScroll;
		pendingInterfaceClosed = false;
		pendingBankTick = client.getTickCount();
		log.debug("Armed ToA Bank-all detection at tick {}: eliteScrollBoxes={}, eliteClueScroll={}",
				pendingBankTick, pendingEliteScrollBoxCount, pendingEliteClueScroll);
	}

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.TOA_CHESTS)
		{
			logRewardItems("ToA rewards interface opened");
		}
	}

	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.TOA_CHESTS)
		{
			return;
		}

		if (hasPendingReward())
		{
			pendingInterfaceClosed = true;
			if (bankTestMode)
			{
				log.debug("[ToA Bank Test] Interface closed immediately with Bank-all pending: itemId={}, count={}, tick={}; continuing to wait for container confirmation",
						BANK_TEST_ITEM_ID, pendingEliteScrollBoxCount, pendingBankTick);
			}
			else
			{
				log.debug("ToA rewards interface closed with Bank-all pending: eliteScrollBoxes={}, eliteClueScroll={}; continuing to wait for container confirmation",
						pendingEliteScrollBoxCount, pendingEliteClueScroll);
			}
		}
		else if (bankTestMode)
		{
			log.debug("[ToA Bank Test] Interface closed with no pending Bank-all test confirmation");
		}
		else
		{
			log.debug("ToA rewards interface closed with no pending elite Bank-all operation");
		}
	}

	public Optional<ToaBankResult> onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.TOA_CHESTS || !hasPendingReward())
		{
			return Optional.empty();
		}

		if (bankTestMode)
		{
			handleTestContainerChange(event.getItemContainer());
			return Optional.empty();
		}

		ClueCounts remaining = clueCounter.getClueCounts(ClueTier.ELITE, event.getItemContainer());
		int bankedBoxes = Math.max(0, pendingEliteScrollBoxCount - remaining.scrollBoxCount());
		boolean bankedClue = pendingEliteClueScroll && !remaining.hasClueScroll();
		log.debug("ToA reward container changed after Bank-all: pendingBoxes={}, remainingBoxes={}, confirmedBoxes={}, pendingClue={}, remainingClue={}, confirmedClue={}",
				pendingEliteScrollBoxCount, remaining.scrollBoxCount(), bankedBoxes,
				pendingEliteClueScroll, remaining.hasClueScroll(), bankedClue);

		ToaBankResult result = new ToaBankResult(bankedBoxes, bankedClue, "ToA container change");
		clearPending("ToA reward container response processed");
		return Optional.of(result);
	}

	public boolean onCommandExecuted(CommandExecuted event)
	{
		if (event.getCommand().equalsIgnoreCase("toabanktest"))
		{
			clearPending("ToA Bank-all test mode toggled");
			bankTestMode = !bankTestMode;
			log.debug("[ToA Bank Test] Test mode {}. Bank-all will {} item {} ('{}') instead of elite clue rewards",
					bankTestMode ? "ENABLED" : "DISABLED",
					bankTestMode ? "track test" : "no longer track test",
					BANK_TEST_ITEM_ID, getTestItemName());
			return true;
		}

		if (event.getCommand().equalsIgnoreCase("toachest"))
		{
			log.debug("Received ::toachest debug command; reading ToA reward container {}", InventoryID.TOA_CHESTS);
			logRewardItems("::toachest command");
			return true;
		}

		return false;
	}

	public void reset(String reason)
	{
		bankTestMode = false;
		clearPending(reason);
		log.debug("Reset ToA chest tracker: {}", reason);
	}

	private void armTestReward(ItemContainer toaRewards)
	{
		int count = toaRewards == null ? 0 : toaRewards.count(BANK_TEST_ITEM_ID);
		log.debug("[ToA Bank Test] Bank-all snapshot: itemId={}, itemName='{}', count={}, rewardContainerAvailable={}, tick={}",
				BANK_TEST_ITEM_ID, getTestItemName(), count, toaRewards != null, client.getTickCount());
		if (count <= 0)
		{
			log.debug("[ToA Bank Test] Bank-all was detected, but test item {} was not present; test confirmation was not armed",
					BANK_TEST_ITEM_ID);
			clearPending("test item was not present when Bank-all was clicked");
			return;
		}

		pendingEliteScrollBoxCount = count;
		pendingEliteClueScroll = false;
		pendingInterfaceClosed = false;
		pendingBankTick = client.getTickCount();
		log.debug("[ToA Bank Test] Armed Bank-all confirmation: itemId={}, count={}, tick={}",
				BANK_TEST_ITEM_ID, pendingEliteScrollBoxCount, pendingBankTick);
	}

	private void handleTestContainerChange(ItemContainer toaRewards)
	{
		int remaining = toaRewards == null ? 0 : toaRewards.count(BANK_TEST_ITEM_ID);
		int confirmed = Math.max(0, pendingEliteScrollBoxCount - remaining);
		log.debug("[ToA Bank Test] Container confirmation received: itemId={}, pending={}, remaining={}, confirmedBanked={}, containerAvailable={}",
				BANK_TEST_ITEM_ID, pendingEliteScrollBoxCount, remaining, confirmed, toaRewards != null);
		if (confirmed > 0)
		{
			Widget toaInterface = client.getWidget(InterfaceID.ToaChests.UNIVERSE);
			boolean visible = toaInterface != null && !toaInterface.isHidden();
			log.debug("[ToA Bank Test] SUCCESS: Bank-all removed {} of test item {} ('{}'); interfaceVisibleAtConfirmation={}",
					confirmed, BANK_TEST_ITEM_ID, getTestItemName(), visible);
		}
		else
		{
			log.debug("[ToA Bank Test] FAILED TO CONFIRM: test item {} did not decrease after Bank-all", BANK_TEST_ITEM_ID);
		}
		clearPending("ToA Bank-all test container response processed");
	}

	private String getTestItemName()
	{
		ItemComposition definition = client.getItemDefinition(BANK_TEST_ITEM_ID);
		return definition == null ? "Unknown item" : definition.getName();
	}

	private void logRewardItems(String source)
	{
		ItemContainer rewards = client.getItemContainer(InventoryID.TOA_CHESTS);
		if (rewards == null)
		{
			log.debug("{}: ToA reward container {} was unavailable", source, InventoryID.TOA_CHESTS);
			return;
		}

		List<String> items = new ArrayList<>();
		for (Item item : rewards.getItems())
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			ItemComposition definition = client.getItemDefinition(item.getId());
			String name = definition == null ? "Unknown item" : definition.getName();
			items.add(String.format(Locale.ENGLISH, "%s (id=%d, quantity=%d)", name, item.getId(), item.getQuantity()));
		}

		if (items.isEmpty())
		{
			log.debug("{}: ToA reward container {} contained no items", source, InventoryID.TOA_CHESTS);
		}
		else
		{
			log.debug("{}: ToA reward container {} contained {} item stack(s): {}",
					source, InventoryID.TOA_CHESTS, items.size(), String.join(", ", items));
		}
	}

	private boolean hasPendingReward()
	{
		return pendingEliteScrollBoxCount > 0 || pendingEliteClueScroll;
	}

	private void clearPending(String reason)
	{
		if (hasPendingReward() || pendingBankTick >= 0)
		{
			log.debug("Clearing pending ToA Bank-all detection (testMode={}, count={}, clueScroll={}, interfaceClosed={}, tick={}): {}",
					bankTestMode, pendingEliteScrollBoxCount, pendingEliteClueScroll,
					pendingInterfaceClosed, pendingBankTick, reason);
		}
		pendingEliteScrollBoxCount = 0;
		pendingEliteClueScroll = false;
		pendingInterfaceClosed = false;
		pendingBankTick = -1;
	}
}
