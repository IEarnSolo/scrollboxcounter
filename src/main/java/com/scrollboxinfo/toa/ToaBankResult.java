package com.scrollboxinfo.toa;

public final class ToaBankResult
{
	private final int eliteScrollBoxCount;
	private final boolean eliteClueScroll;
	private final String confirmationSource;

	public ToaBankResult(int eliteScrollBoxCount, boolean eliteClueScroll, String confirmationSource)
	{
		this.eliteScrollBoxCount = eliteScrollBoxCount;
		this.eliteClueScroll = eliteClueScroll;
		this.confirmationSource = confirmationSource;
	}

	public int getEliteScrollBoxCount()
	{
		return eliteScrollBoxCount;
	}

	public boolean hasEliteClueScroll()
	{
		return eliteClueScroll;
	}

	public String getConfirmationSource()
	{
		return confirmationSource;
	}
}
