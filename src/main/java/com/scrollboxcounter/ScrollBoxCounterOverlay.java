package com.scrollboxcounter;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.ItemContainer;
import net.runelite.api.Item;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ScrollBoxCounterOverlay extends WidgetItemOverlay
{
	private final ScrollBoxCounterConfig config;
	private final Client client;
	private final ScrollBoxCounterPlugin plugin;
	private int currentItemId;
	private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
	private boolean initializedInventoryTracking = false;

	@Inject
	ScrollBoxCounterOverlay(ScrollBoxCounterConfig config, Client client, ScrollBoxCounterPlugin plugin)
	{
		this.config = config;
		this.client = client;
		this.plugin = plugin;
		showOnInventory();
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!ScrollBoxCounterUtils.isClueScrollBox(itemId))
		{
			return;
		}

		this.currentItemId = itemId;

		if (!initializedInventoryTracking) {
			updatePreviousInventoryCounts();
			initializedInventoryTracking = true;
		}

		if (!isItemInBank(widgetItem)) {
			checkInventoryChanges();
		}

		final Rectangle bounds = widgetItem.getCanvasBounds();
		if (bounds == null)
		{
			return;
		}

		int quantity = widgetItem.getQuantity();
		if (quantity < 1)
		{
			return;
		}

		boolean isInBank = isItemInBank(widgetItem);
		int maxClues = ScrollBoxCounterUtils.getMaxClueCount(itemId, client);
		int bankCount = plugin.getBankCount(itemId);

		int totalCount = quantity + (isInBank ? 0 : bankCount);
		if (isInBank)
		{
			int inventoryCount = ScrollBoxCounterUtils.getInventoryCount(itemId, client);
			totalCount = quantity + inventoryCount;
		}

		boolean isFullStack = config.markFullStacks() && totalCount >= maxClues;

		if (config.maxCluePosition() == ScrollBoxCounterConfig.MaxCluePosition.DISABLED && !config.showBanked() && !isFullStack)
		{
			return;
		}

		renderOverlayText(graphics, bounds, quantity, maxClues, isFullStack, isInBank);
	}

	private boolean isItemInBank(WidgetItem widgetItem)
	{
		return widgetItem.getWidget().getId() >>> 16 == 12;
	}

	private void renderOverlayText(Graphics2D graphics, Rectangle bounds, int quantity, int maxClues, boolean isFullStack, boolean isInBank)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());
		Color textColor = isFullStack ? Color.RED : Color.WHITE;

		if (isFullStack)
		{
			renderQuantityOnly(graphics, bounds, quantity, textColor);
		}

		if (config.maxCluePosition() != ScrollBoxCounterConfig.MaxCluePosition.DISABLED)
		{
			renderMaxClues(graphics, bounds, maxClues, textColor, config.maxCluePosition());
		}

		if (config.showBanked() && !isInBank)
		{
			int bankCount = plugin.getBankCount(currentItemId);
			if (bankCount > 0)
			{
				renderBankedQuantity(graphics, bounds, bankCount, textColor);
			}
		}
	}

	private void renderQuantityOnly(Graphics2D graphics, Rectangle bounds, int quantity, Color textColor)
	{
		String quantityText = String.valueOf(quantity);
		graphics.setColor(textColor);
		graphics.drawString(quantityText, bounds.x, bounds.y + 10);
	}

	private void renderMaxClues(Graphics2D graphics, Rectangle bounds, int maxClues, Color textColor, ScrollBoxCounterConfig.MaxCluePosition position)
	{
		String maxText = String.valueOf(maxClues);
		FontMetrics fm = graphics.getFontMetrics();
		int textWidth = fm.stringWidth(maxText);

		int x;
		int y;
		switch (position)
		{
			case BOTTOM_LEFT:
				x = bounds.x;
				y = bounds.y + bounds.height - 2;
				break;
			case BOTTOM_RIGHT:
				x = bounds.x + bounds.width - textWidth;
				y = bounds.y + bounds.height - 2;
				break;
			default:
				return;
		}

		graphics.setColor(Color.BLACK);
		graphics.drawString(maxText, x + 1, y + 1);

		graphics.setColor(textColor);
		graphics.drawString(maxText, x, y);
	}

	private void renderBankedQuantity(Graphics2D graphics, Rectangle bounds, int bankCount, Color textColor)
	{
		String bankedText = "+" + bankCount;
		FontMetrics fm = graphics.getFontMetrics();

		int x = bounds.x + bounds.width - fm.stringWidth(bankedText);
		int y = bounds.y + 10;

		graphics.setColor(Color.BLACK);
		graphics.drawString(bankedText, x + 1, y + 1);

		graphics.setColor(textColor);
		graphics.drawString(bankedText, x, y);
	}

	private void checkInventoryChanges() {
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null) {
			return;
		}

		Map<Integer, Integer> currentCounts = new HashMap<>();
		for (Item item : inventory.getItems()) {
			if (item != null && item.getId() != -1 && ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
				currentCounts.put(item.getId(), item.getQuantity());
			}
		}

		for (Map.Entry<Integer, Integer> entry : currentCounts.entrySet()) {
			int itemId = entry.getKey();
			int currentCount = entry.getValue();
			int previousCount = previousInventoryCounts.getOrDefault(itemId, 0);

			if (currentCount > previousCount) {
				if (!ScrollBoxCounterUtils.isBankOpen(client) && config.showChatMessages()) {
					sendScrollBoxMessage(itemId, currentCount);
				}
			}
		}

		previousInventoryCounts.clear();
		previousInventoryCounts.putAll(currentCounts);
	}

	private void sendScrollBoxMessage(int itemId, int inventoryCount) {
		String tierName = ScrollBoxCounterUtils.getScrollBoxTierName(itemId);
		int bankCount = plugin.getBankCount(itemId);
		int totalCount = inventoryCount + bankCount;
		int maxCount = ScrollBoxCounterUtils.getMaxClueCount(itemId, client);

		String message = "Holding " + totalCount + "/" + maxCount + " scroll boxes (" + tierName + ")";
		plugin.sendChatMessage(message);
	}

	private void updatePreviousInventoryCounts() {
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null) {
			return;
		}

		previousInventoryCounts.clear();
		for (Item item : inventory.getItems()) {
			if (item != null && item.getId() != -1 && ScrollBoxCounterUtils.isClueScrollBox(item.getId())) {
				previousInventoryCounts.put(item.getId(), item.getQuantity());
			}
		}
	}
}
