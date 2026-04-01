package com.concord.servers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClanServer
{
	NONE("(None)", ""),
	LOCAL_HOST("LocalHost", "wss://localhost"),
	SCAPE_SOCIETY("ScapeSociety", "wss://scapesociety.net");

	private final String clanName;
	private final String url;

	public String getClanName()
	{
		return clanName;
	}
	
	public String getUrl()
	{
		return url;
	}

	@Override
	public String toString()
	{
		return clanName;
	}
}
