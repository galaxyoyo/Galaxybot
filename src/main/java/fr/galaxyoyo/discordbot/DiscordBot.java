package fr.galaxyoyo.discordbot;

import fr.galaxyoyo.discordbot.commands.CommandListener;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class DiscordBot
{
	public static Logger logger;
	private static DiscordClientImpl client;

	public static void main(String... args) throws DiscordException, UnsupportedEncodingException
	{
		String date = new SimpleDateFormat("kk-mm-ss-dd-MM-yyyy").format(new Date());

		logger = Logger.getLogger("Galaxybot-" + date);
		FileHandler fh;

//		System.out.println(Arrays.toString(FindRootsCommand.findRoots3Degrees(new ComplexNumber(6), new ComplexNumber(11), new ComplexNumber(6))));
		//	System.exit(0);

		try
		{
			String path = "logs/" + date + ".log";
			fh = new FileHandler(path);
			logger.addHandler(fh);
			fh.setFormatter(new LogFormatter());
			logger.info("Starting.");

		}
		catch (SecurityException | IOException ex)
		{
			ex.printStackTrace();
		}

		client = (DiscordClientImpl) new ClientBuilder()
				//	.withLogin("yohann.danello@gmail.com", "84696812yD")
				.withToken("MTg3NTMwMDc3ODYzNTQyNzg1.CjBcDg.SDnhspfi3peGDnamowSB3pe2t58")
				.setDaemon(false).login();
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener());
		dispatcher.registerListener(new CommandListener());
		dispatcher.registerListener(new MentionListener());

		System.setSecurityManager(new GalaxySecurityManager());
	}

	public static void sendTemporaryMessage(String content, Channel channel, long removeIn)
	{
		content += "\n_Ce message s'auto-détruira dans " + (removeIn / 1000) + " secondes._";
		try
		{
			Message msg = (Message) channel.sendMessage(content);
			Executors.newSingleThreadExecutor().execute(() ->
			{
				try
				{
					Thread.sleep(removeIn);
					msg.delete();
				}
				catch (InterruptedException | DiscordException | RateLimitException | MissingPermissionsException e)
				{
					e.printStackTrace();
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static DiscordClientImpl getClient()
	{
		return client;
	}

	public static boolean checkPermission(Channel channel, Permissions perm)
	{
		return channel.isPrivate() || channel.getModifiedPermissions(client.getOurUser()).contains(perm);

	}
}
