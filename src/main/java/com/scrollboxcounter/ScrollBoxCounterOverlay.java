package com.scrollboxcounter;

import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;

public class ScrollBoxCounterOverlay extends WidgetItemOverlay
{
	@Getter
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
		if (plugin.isUsingPendingBankItems() && !ScrollBoxCounterUtils.isClueScrollBox(itemId)) {
			return;
		}
		if (!ScrollBoxCounterUtils.isClueScrollBox(itemId))
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
		int maxClues = ScrollBoxCounterUtils.getMaxClueCount(itemId, client);
		int bankCount = plugin.getOverlayBankCount(itemId);
		int activeClueScrolls = ScrollBoxCounterUtils.getActiveClueScrollCount(itemId, client, plugin.getItemManager(), plugin);

		int totalCount = quantity + (isInBank ? 0 : bankCount);
		if (isInBank)
		{
			int inventoryCount = ScrollBoxCounterUtils.getInventoryCount(itemId, client);
			totalCount = quantity + inventoryCount;
		}

		boolean isFullStack = config.markFullStacks() && (totalCount + activeClueScrolls) >= maxClues;

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

		if (config.showBanked() && !isInBank && plugin.hasVisitedBank())
		{
			int bankCount = plugin.getOverlayBankCount(currentItemId);
			int bankActiveClues = plugin.getBankActiveClueScrollCount(currentItemId);
			int totalBanked = bankCount + bankActiveClues;
			if (totalBanked > 0)
			{
				renderBankedQuantity(graphics, bounds, totalBanked, textColor);
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

}
