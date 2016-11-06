package fr.galaxyoyo.discordbot;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Status;

import java.util.concurrent.Executors;

public class ReadyListener implements IListener<ReadyEvent>
{
	@Override
	public void handle(ReadyEvent event)
	{
		Executors.newSingleThreadExecutor().execute(() ->
		{
			try
			{
				event.getClient().changeUsername("Galaxybot");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			event.getClient().changeStatus(Status.game("dessiner des p*i*s (pains)"));
			DiscordBot.logger.info("Bot started!");

			// DiscordBot.getClient().getGuildByID("227476905723559936").getRoles().forEach(role -> System.out.println(role.getName() + ": " + role.getID()));
		});
	}
}
