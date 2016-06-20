package fr.galaxyoyo.discordbot;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;

import java.util.Optional;
import java.util.concurrent.Executors;

public class ReadyListener implements IListener<ReadyEvent>
{
	@Override
	public void handle(ReadyEvent event)
	{
		Executors.newSingleThreadExecutor().execute(() ->
		{
			while (DiscordBot.getSdd() == null)
			{
				DiscordBot.setSdd((Guild) event.getClient().getGuildByID("186941943941562369"));
				try
				{
					Thread.sleep(50L);
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			try
			{
				event.getClient().changeUsername("Galaxybot");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			event.getClient().updatePresence(false, Optional.of("Démineur"));
			DiscordBot.setBotChannel((Channel) event.getClient().getChannelByID("186943017746300928"));
			DiscordBot.logger.info("Bot started!");
		});
	}
}
