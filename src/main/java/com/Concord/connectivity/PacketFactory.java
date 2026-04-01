package com.concord.connectivity;

public final class PacketFactory
{
	public ConcordPacket buildChatMessagePacket(String userId, String sessionToken, String name, String message)
	{
		ConcordPacket packet = new ConcordPacket();
		packet.setType("chat.message");
		packet.setOrigin("runelite");

		ConcordPacket.Actor actor = new ConcordPacket.Actor();
		actor.id = userId;
		actor.name = name;
		packet.setActor(actor);

		ConcordPacket.Auth auth = new ConcordPacket.Auth();
		auth.userId = userId;
		auth.sessionToken = sessionToken;
		packet.setAuth(auth);

		ConcordPacket.Payload payload = new ConcordPacket.Payload();
		payload.body = message;
		packet.setData(payload);

		return packet;
	}

	public ConcordPacket buildProfileSyncPacket(String userId, String sessionToken, String osrsName)
	{
		ConcordPacket packet = new ConcordPacket();
		packet.setType("auth.profileSync");
		packet.setOrigin("runelite");

		ConcordPacket.Actor actor = new ConcordPacket.Actor();
		actor.id = userId;
		actor.name = osrsName;
		packet.setActor(actor);

		ConcordPacket.Auth auth = new ConcordPacket.Auth();
		auth.userId = userId;
		auth.sessionToken = sessionToken;
		packet.setAuth(auth);

		ConcordPacket.Payload payload = new ConcordPacket.Payload();
		payload.osrsName = osrsName;
		packet.setData(payload);

		return packet;
	}

	public ConcordPacket buildResumePacket(String userId, String sessionToken, String osrsName)
	{
		ConcordPacket packet = new ConcordPacket();
		packet.setType("auth.resume");
		packet.setOrigin("runelite");

		ConcordPacket.Actor actor = new ConcordPacket.Actor();
		actor.id = userId;
		actor.name = osrsName;
		packet.setActor(actor);

		ConcordPacket.Auth auth = new ConcordPacket.Auth();
		auth.userId = userId;
		auth.sessionToken = sessionToken;
		packet.setAuth(auth);

		ConcordPacket.Payload payload = new ConcordPacket.Payload();
		payload.userId = userId;
		payload.sessionToken = sessionToken;
		payload.osrsName = osrsName;
		packet.setData(payload);

		return packet;
	}
}
