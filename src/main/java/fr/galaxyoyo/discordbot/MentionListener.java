package fr.galaxyoyo.discordbot;

import fr.galaxyoyo.discordbot.commands.CommandListener;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.handle.impl.obj.Message;

public class MentionListener implements IListener<MentionEvent>
{
	@Override
	public void handle(MentionEvent event)
	{
		Message msg = (Message) event.getMessage();
		if (msg.getContent().startsWith(String.valueOf(CommandListener.PREFIX)) || msg.getContent().toLowerCase().contains("here") || msg.mentionsEveryone())
			return;

		if (!msg.getAttachments().isEmpty())
			return;

		if (msg.getAuthor() == DiscordBot.getClient().getOurUser())
			return;

		try
		{
			msg.reply("42");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
