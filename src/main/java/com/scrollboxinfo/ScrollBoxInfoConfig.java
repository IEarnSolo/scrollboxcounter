package com.scrollboxinfo;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("scrollboxinfo")
public interface ScrollBoxInfoConfig extends Config
{
	public enum TextPosition
	{
		OFF,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	public enum ClueScrollOverlay
	{
		OFF,
		ONLY_NUMBERS,
		ONLY_TIER_LABEL,
		BOTH
	}

	@ConfigSection(
			name = "Tooltip overlay",
			description = "Customize how clue scroll information is shown in the tooltip overlay",
			position = 0
	)
	String tooltipOverlay = "tooltipOverlay";

	@ConfigSection(
			name = "Item overlay",
			description = "Customize how clue scroll information is shown in the item overlay",
			position = 100
	)
	String itemOverlay = "itemOverlay";

	@ConfigSection(
			name = "Infobox",
			description = "Customize how clue scroll information is shown in the infobox",
			position = 200
	)
	String infobox = "infobox";

	@ConfigSection(
			name = "Chat message",
			description = "Customize how chat messages are sent",
			position = 300
	)
	String chatMessage = "chatMessage";

	@ConfigSection(
			name = "Menu option",
			description = "Customize which menu options are displayed",
			position = 400
	)
	String menuOption = "menuOption";

	// ===== Tooltip overlay =====

	@ConfigItem(
			keyName = "showBanked",
			name = "Show banked",
			description = "Display the number of scroll boxes and clues banked",
			position = 1,
			section = tooltipOverlay
	)
	default boolean showBanked()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showCurrent",
			name = "Show current total",
			description = "Display the total number of scroll boxes and clue scrolls currently owned",
			position = 2,
			section = tooltipOverlay
	)
	default boolean showCurrent()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showCap",
			name = "Show stack limit",
			description = "Display the stack limit amount of how many scroll boxes you can hold of the same tier",
			position = 3,
			section = tooltipOverlay
	)
	default boolean showCap()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showNextUnlock",
			name = "Show next increase",
			description = "Display how many clue completions until next stack limit increase",
			position = 4,
			section = tooltipOverlay
	)
	default boolean showNextUnlock()
	{
		return true;
	}

	// ===== Item overlay =====

	@ConfigItem(
			keyName = "markFullStack",
			name = "Mark full stacks red",
			description = "Mark the scroll box amount red when you’ve hit your stack limit",
			position = 101,
			section = itemOverlay
	)
	default boolean markFullStack()
	{
		return true;
	}

	@ConfigItem(
			name = "Show tier label",
			keyName = "showTierLabel",
			description = "Show the clue tier name on clue items",
			position = 102,
			section = itemOverlay
	)
	default boolean showTierLabel()
	{
		return true;
	}

	@ConfigItem(
			keyName = "useTierShortnames",
			name = "Use tier shortnames",
			description = "Use shortnames for tier labels",
			position = 103,
			section = itemOverlay
	)
	default boolean useTierShortnames()
	{
		return false;
	}


	@ConfigItem(
			name = "Color tier label",
			keyName = "colorTierLabel",
			description = "Color the tier labels over clue items",
			position = 104,
			section = itemOverlay
	)
	default boolean colorTierLabel()
	{
		return true;
	}

	@ConfigItem(
			keyName = "beginnerTierColor",
			name = "Beginner tier color",
			description = "Text color for beginner clues",
			position = 105,
			section = itemOverlay
	)
	default Color beginnerTierColor() {
		return new Color(0xc3bbba);
	}

	@ConfigItem(
			keyName = "easyTierColor",
			name = "Easy tier color",
			description = "Text color for easy clues",
			position = 106,
			section = itemOverlay
	)
	default Color easyTierColor() {
		return new Color(0x2b952f);
	}

	@ConfigItem(
			keyName = "mediumTierColor",
			name = "Medium tier color",
			description = "Text color for medium clues",
			position = 107,
			section = itemOverlay
	)
	default Color mediumTierColor() {
		return new Color(0x5ea4a7);
	}

	@ConfigItem(
			keyName = "hardTierColor",
			name = "Hard tier color",
			description = "Text color for hard clues",
			position = 108,
			section = itemOverlay
	)
	default Color hardTierColor() {
		return new Color(0xc870e0);
	}

	@ConfigItem(
			keyName = "eliteTierColor",
			name = "Elite tier color",
			description = "Text color for elite clues",
			position = 109,
			section = itemOverlay
	)
	default Color eliteTierColor() {
		return new Color(0xc2aa18);
	}

	@ConfigItem(
			keyName = "masterTierColor",
			name = "Master tier color",
			description = "Text color for master clues",
			position = 110,
			section = itemOverlay
	)
	default Color masterTierColor() {
		return new Color(0xa7342a);
	}

	@ConfigItem(
			keyName = "showBankedPosition",
			name = "Show banked",
			description = "Position of the banked count text",
			position = 111,
			section = itemOverlay
	)
	default TextPosition showBankedPosition()
	{
		return TextPosition.OFF;
	}

	@ConfigItem(
			keyName = "showCurrentTotalPosition",
			name = "Show current total",
			description = "Position of the current total count text",
			position = 112,
			section = itemOverlay
	)
	default TextPosition showCurrentTotalPosition()
	{
		return TextPosition.OFF;
	}

	@ConfigItem(
			keyName = "showStackLimitPosition",
			name = "Show stack limit",
			description = "Position of the stack limit text",
			position = 113,
			section = itemOverlay
	)
	default TextPosition showStackLimitPosition()
	{
		return TextPosition.OFF;
	}

	@ConfigItem(
			keyName = "showClueScrollOverlay",
			name = "Show on clue scrolls",
			description = "Show or hide overlays on clue scrolls",
			position = 114,
			section = itemOverlay
	)
	default ClueScrollOverlay showClueScrollOverlay()
	{
		return ClueScrollOverlay.OFF;
	}

	// ===== Infobox =====

	@ConfigItem(
			keyName = "showFullStackInfobox",
			name = "Show full stack",
			description = "Display an infobox when you've reached your clue stack limit",
			position = 201,
			section = infobox
	)
	default boolean showFullStackInfobox()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showBeginnerInfobox",
			name = "Show beginner",
			description = "Show infobox for beginner clues",
			position = 202,
			section = infobox
	)
	default boolean showBeginnerInfobox() { return true; }

	@ConfigItem(
			keyName = "showEasyInfobox",
			name = "Show easy",
			description = "Show infobox for easy clues",
			position = 203,
			section = infobox
	)
	default boolean showEasyInfobox() { return true; }

	@ConfigItem(
			keyName = "showMediumInfobox",
			name = "Show medium",
			description = "Show infobox for medium clues",
			position = 204,
			section = infobox
	)
	default boolean showMediumInfobox() { return true; }

	@ConfigItem(
			keyName = "showHardInfobox",
			name = "Show hard",
			description = "Show infobox for hard clues",
			position = 205,
			section = infobox
	)
	default boolean showHardInfobox() { return true; }

	@ConfigItem(
			keyName = "showEliteInfobox",
			name = "Show elite",
			description = "Show infobox for elite clues",
			position = 206,
			section = infobox
	)
	default boolean showEliteInfobox() { return true; }

	@ConfigItem(
			keyName = "showMasterInfobox",
			name = "Show master",
			description = "Show infobox for master clues",
			position = 207,
			section = infobox
	)
	default boolean showMasterInfobox() { return true; }

	// ===== Chat message =====

	@ConfigItem(
			keyName = "showChatMessage",
			name = "Show chat message",
			description = "Send a chat message of your current scroll box/clue scroll total when a scroll box is received",
			position = 301,
			section = chatMessage
	)
	default boolean showChatMessage()
	{
		return true;
	}

	// ===== Menu option =====

	@ConfigItem(
			keyName = "showInventoryRightClickOption",
			name = "Show clue counts menu option",
			description = "Show 'View clue counts' menu option in the inventory tab right-click menu",
			position = 401,
			section = menuOption
	)
	default boolean showInventoryRightClickOption()
	{
		return true;
	}

	@ConfigItem(
			keyName = "lastSeenChangelogVersion",
			name = "lastSeenChangelogVersion",
			description = "",
			hidden = true
			//position = 800
	)
	default String lastSeenChangelogVersion()
	{
		return "1.0.0"; // Temporary default value to send out first changelog messages.
		// TODO: Set to "" later, so new installs won't receive changelog messages that are irrelevant to new users
	}

	@ConfigItem(
			keyName = "lastSeenChangelogVersion",
			name = "",
			description = "",
			hidden = true
	)
	void setLastSeenChangelogVersion(String value);

}