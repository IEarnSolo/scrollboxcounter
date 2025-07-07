package com.scrollboxcounter;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;

/**
 * Overlay that displays clue scroll box counts and maximum capacity on items.
 */
public class ScrollBoxCounterOverlay extends WidgetItemOverlay
{
	private static final int BASE_CLUE_COUNT = 2;

	private final ScrollBoxCounterConfig config;
	private final Client client;
	private final ScrollBoxCounterPlugin plugin;
	private int currentItemId;

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

		this.currentItemId = itemId;

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
		if (isInBank)
		{
			int inventoryCount = getInventoryCount(itemId);
			totalCount = quantity + inventoryCount;
		}

		boolean isFullStack = config.markFullStacks() && totalCount >= maxClues;

		// Skip rendering if nothing to show
		if (config.maxCluePosition() == ScrollBoxCounterConfig.MaxCluePosition.DISABLED && !config.showBanked() && !isFullStack)
		{
			return;
		}

		renderOverlayText(graphics, bounds, quantity, maxClues, isFullStack, isInBank);
	}

	/**
	 * Checks if the widget item is displayed in the bank interface.
	 */
	private boolean isItemInBank(WidgetItem widgetItem)
	{
		return widgetItem.getWidget().getId() >>> 16 == 12;
	}

	/**
	 * Renders all overlay text elements for the item.
	 */
	private void renderOverlayText(Graphics2D graphics, Rectangle bounds, int quantity, int maxClues, boolean isFullStack, boolean isInBank)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());
		Color textColor = isFullStack ? Color.RED : Color.WHITE;

		// Only render quantity when stack is full (red highlighting)
		if (isFullStack)
		{
			renderQuantityOnly(graphics, bounds, quantity, textColor);
		}

		// Show max clues counter
		if (config.maxCluePosition() != ScrollBoxCounterConfig.MaxCluePosition.DISABLED)
		{
			renderMaxClues(graphics, bounds, maxClues, textColor, config.maxCluePosition());
		}

		// Show banked quantity in inventory
		if (config.showBanked() && !isInBank)
		{
			int bankCount = plugin.getBankCount(currentItemId);
			if (bankCount > 0)
			{
				renderBankedQuantity(graphics, bounds, bankCount, textColor);
			}
		}
	}

	/**
	 * Renders the item quantity in the top-left corner.
	 */
	private void renderQuantityOnly(Graphics2D graphics, Rectangle bounds, int quantity, Color textColor)
	{
		String quantityText = String.valueOf(quantity);
		graphics.setColor(textColor);
		graphics.drawString(quantityText, bounds.x, bounds.y + 10);
	}

	/**
	 * Renders the maximum clue count at the specified position.
	 */
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

		// Draw shadow for better visibility
		graphics.setColor(Color.BLACK);
		graphics.drawString(maxText, x + 1, y + 1);

		graphics.setColor(textColor);
		graphics.drawString(maxText, x, y);
	}

	/**
	 * Renders the banked quantity in the top-right corner.
	 */
	private void renderBankedQuantity(Graphics2D graphics, Rectangle bounds, int bankCount, Color textColor)
	{
		String bankedText = "+" + bankCount;
		FontMetrics fm = graphics.getFontMetrics();

		int x = bounds.x + bounds.width - fm.stringWidth(bankedText);
		int y = bounds.y + 10;

		// Draw shadow for better visibility
		graphics.setColor(Color.BLACK);
		graphics.drawString(bankedText, x + 1, y + 1);

		graphics.setColor(textColor);
		graphics.drawString(bankedText, x, y);
	}

	/**
	 * Calculates the maximum clue count based on scroll case upgrades.
	 */
	private int getMaxClueCount(int itemId)
	{
        int tierBonus = getTierBonus(itemId);
		int mimicBonus = getMimicBonus();

		return BASE_CLUE_COUNT + tierBonus + mimicBonus;
	}

	/**
	 * Gets the tier-specific bonus from scroll case upgrades.
	 */
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

	/**
	 * Gets the mimic bonus from scroll case upgrades.
	 */
	private int getMimicBonus()
	{
		return client.getVarbitValue(ScrollBoxCounterPlugin.SCROLL_CASE_MIMIC);
	}

	/**
	 * Gets the inventory count for a specific item.
	 */
	private int getInventoryCount(int itemId)
	{
		net.runelite.api.ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return 0;
		}
		return inventory.count(itemId);
	}
}
