package fr.galaxyoyo.discordbot;

import fr.galaxyoyo.discordbot.commands.CommandListener;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.EventDispatcher;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class DiscordBot
{
	public static Logger logger;
	private static Guild sdd;
	private static Channel botChannel;
	private static DiscordClientImpl client;

	public static void main(String... args) throws DiscordException, UnsupportedEncodingException, HTTP429Exception
	{
		String date = new SimpleDateFormat("kk-mm-ss-dd-MM-yyyy").format(new Date());

		logger = Logger.getLogger("Samaritan-" + date);
		FileHandler fh;

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
				//	.withLogin(EMAIL, PASSWORD)
				.withToken("MTg3NTMwMDc3ODYzNTQyNzg1.CjBcDg.SDnhspfi3peGDnamowSB3pe2t58")
				.setDaemon(false).login();
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener());
		dispatcher.registerListener(new CommandListener());
	}

	public static void sendTemporaryMessage(String content, long removeIn)
	{
		content += "\n_Ce message s'auto-détruira dans " + (removeIn / 1000) + " secondes._";
		try
		{
			Message msg = (Message) botChannel.sendMessage(content);
			Executors.newSingleThreadExecutor().execute(() ->
			{
				try
				{
					Thread.sleep(removeIn);
					msg.delete();
				}
				catch (InterruptedException | DiscordException | HTTP429Exception | MissingPermissionsException e)
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

	public static Guild getSdd()
	{
		return sdd;
	}

	protected static void setSdd(Guild sdd)
	{
		DiscordBot.sdd = sdd;
	}

	public static void setBotChannel(Channel botChannel)
	{
		DiscordBot.botChannel = botChannel;
	}

	public static DiscordClientImpl getClient()
	{
		return client;
	}

	public static Message sendMessage(Channel channel, String content, String... mentions) throws MissingPermissionsException, HTTP429Exception, DiscordException
	{
		return sendMessage(channel, content, false, mentions);
	}

	public static Message sendMessage(Channel channel, String content, boolean tts, String... mentions) throws MissingPermissionsException, HTTP429Exception, DiscordException
	{
		DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.SEND_MESSAGES));

		if (client.isReady())
		{
			try
			{
				MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages",
						new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, mentions, tts)), "UTF-8"),
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);

				return (Message) DiscordUtils.getMessageFromJSON(client, channel, response);
			}
			catch (DiscordException ex)
			{
				return sendMessage(channel, content, tts, mentions);
			}

		}
		else
		{
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}
}
