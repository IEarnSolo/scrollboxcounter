package com.scrollboxcounter;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;

public class ScrollBoxCounterOverlay extends WidgetItemOverlay
{
	private static final int BASE_CLUE_COUNT = 2;

	private final ScrollBoxCounterConfig config;
	private final Client client;

	@Inject
	ScrollBoxCounterOverlay(ScrollBoxCounterConfig config, Client client)
	{
		this.config = config;
		this.client = client;
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

		int maxClues = getMaxClueCount(itemId);
		boolean isFullStack = config.markFullStacks() && quantity >= maxClues;

		if (!config.showCounter() && !isFullStack)
		{
			return;
		}

		renderOverlayText(graphics, bounds, quantity, maxClues, isFullStack);
	}

	private void renderOverlayText(Graphics2D graphics, Rectangle bounds, int quantity, int maxClues, boolean isFullStack)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());
		Color textColor = isFullStack ? Color.RED : Color.YELLOW;

		if (isFullStack && !config.showCounter())
		{
			renderQuantityOnly(graphics, bounds, quantity, textColor);
		}
		else if (config.showCounter())
		{
			renderQuantityAndMax(graphics, bounds, quantity, maxClues, textColor);
		}
	}

	private void renderQuantityOnly(Graphics2D graphics, Rectangle bounds, int quantity, Color textColor)
	{
		String quantityText = String.valueOf(quantity);
		graphics.setColor(textColor);
		graphics.drawString(quantityText, bounds.x, bounds.y + 10);
	}

	private void renderQuantityAndMax(Graphics2D graphics, Rectangle bounds, int quantity, int maxClues, Color textColor)
	{
		String quantityText = String.valueOf(quantity);
		graphics.setColor(textColor);
		graphics.drawString(quantityText, bounds.x, bounds.y + 10);

		FontMetrics fm = graphics.getFontMetrics();
		int quantityWidth = fm.stringWidth(quantityText);
		String additionalText = "/" + maxClues;
		int x = bounds.x + quantityWidth + 1;
		int y = bounds.y + 10;

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(additionalText, x + 1, y + 1);

		// Draw main text
		graphics.setColor(textColor);
		graphics.drawString(additionalText, x, y);
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
}

