package com.scrollboxcounter;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;

public class ScrollBoxCounterOverlay extends WidgetItemOverlay
{
	private static final int BASE_CLUE_COUNT = 2;

	private final ScrollBoxCounterConfig config;
	private final Client client;
	private final ScrollBoxCounterPlugin plugin;
	private int currentItemId; // Store current item ID during rendering

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
		if (!ScrollBoxCounterPlugin.isClueScrollBox(itemId))
		{
			return;
		}

		this.currentItemId = itemId; // Store for use in helper methods

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
		int maxClues = getMaxClueCount(itemId);
		int bankCount = plugin.getBankCount(itemId);

		// Calculate total count for color determination
		int totalCount = quantity + (isInBank ? 0 : bankCount);
		// If we're in bank, we need to add inventory count for full stack check
		if (isInBank)
		{
			// Get inventory count for this item
			int inventoryCount = getInventoryCount(itemId);
			totalCount = quantity + inventoryCount;
		}

		boolean isFullStack = config.markFullStacks() && totalCount >= maxClues;

		// Always show something if we have items or if counter/banked is enabled
		if (config.maxCluePosition() == ScrollBoxCounterConfig.MaxCluePosition.DISABLED && !config.showBanked() && !isFullStack)
		{
			return;
		}

		renderOverlayText(graphics, bounds, quantity, maxClues, isFullStack, isInBank);
	}

	private boolean isItemInBank(WidgetItem widgetItem)
	{
		// Check if the widget is from the bank interface
		return widgetItem.getWidget().getId() >>> 16 == 12; // Bank widget group ID is 12
	}

	private void renderOverlayText(Graphics2D graphics, Rectangle bounds, int quantity, int maxClues, boolean isFullStack, boolean isInBank)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());
		Color textColor = isFullStack ? Color.RED : Color.WHITE;

		// Always show quantity if full stack is reached
		if (isFullStack)
		{
			renderQuantityOnly(graphics, bounds, quantity, textColor);
		}

		if (config.maxCluePosition() != ScrollBoxCounterConfig.MaxCluePosition.DISABLED)
		{
			// Render max clues in selected position
			renderMaxClues(graphics, bounds, maxClues, textColor, config.maxCluePosition());
		}

		// Show banked quantity if enabled and NOT in bank (independent of showCounter)
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

		int x, y;
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
				return; // DISABLED case
		}

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(maxText, x + 1, y + 1);

		// Draw main text
		graphics.setColor(textColor);
		graphics.drawString(maxText, x, y);
	}

	private void renderBankedQuantity(Graphics2D graphics, Rectangle bounds, int bankCount, Color textColor)
	{
		String bankedText = "+" + bankCount;
		FontMetrics fm = graphics.getFontMetrics();

		// Position at top-right of the item
		int x = bounds.x + bounds.width - fm.stringWidth(bankedText);
		int y = bounds.y + 10;

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(bankedText, x + 1, y + 1);

		// Draw main text
		graphics.setColor(textColor);
		graphics.drawString(bankedText, x, y);
	}

	private int getItemIdFromBounds(Rectangle bounds)
	{
		// This is a helper method to get itemId from current render context
		// Since we need the itemId in renderBankedQuantity, we'll need to modify the render chain
		return currentItemId;
	}

	private int getMaxClueCount(int itemId)
	{
        int tierBonus = getTierBonus(itemId);
		int mimicBonus = getMimicBonus();

		return BASE_CLUE_COUNT + tierBonus + mimicBonus;
	}

	private int getTierBonus(int itemId)
	{
		int bonus = 0;

		switch (itemId)
		{
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_BEGINNER:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_BEGINNER_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_BEGINNER_MAJOR);
				break;
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_EASY:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_EASY_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_EASY_MAJOR);
				break;
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MEDIUM:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MEDIUM_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MEDIUM_MAJOR);
				break;
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_HARD:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_HARD_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_HARD_MAJOR);
				break;
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_ELITE:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_ELITE_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_ELITE_MAJOR);
				break;
			case ScrollBoxCounterPlugin.CLUE_SCROLL_BOX_MASTER:
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MASTER_MINOR);
				bonus += client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MASTER_MAJOR);
				break;
        }

		return bonus;
	}

	private int getMimicBonus()
	{
		return client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MIMIC);
	}

	private int getInventoryCount(int itemId)
	{
		// Get inventory count for the given item
		net.runelite.api.ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return 0;
		}
		return inventory.count(itemId);
	}
}
