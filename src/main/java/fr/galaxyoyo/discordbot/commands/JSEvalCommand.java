package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import fr.galaxyoyo.discordbot.Survey;
import fr.galaxyoyo.discordbot.utils.Messages;
import fr.galaxyoyo.discordbot.utils.Users;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.Presences;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.MessageBuilder;

import javax.imageio.ImageIO;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class JSEvalCommand implements Command
{
	private static final ScriptEngineManager factory = new ScriptEngineManager();
	private static final NashornScriptEngine engine = (NashornScriptEngine) factory.getEngineByName("nashorn");

	static
	{
		try
		{
			engine.put("client", DiscordBot.getClient());
			engine.put("engine", engine);
			Class<?>[] classes = new Class<?>[]{Math.class, Number.class, Byte.class, Integer.class, Double.class, Float.class, Long.class, Character.class, String.class,
					List.class, ArrayList.class, Map.class, HashMap.class, Date.class, Calendar.class, GregorianCalendar.class, System.class, Runnable.class, Thread.class,
					File.class, Arrays.class, BufferedImage.class, Graphics2D.class, ImageIO.class, Random.class,
					Messages.class, Users.class, Presences.class, Status.class, JSEvalCommand.class, Survey.class, DiscordUtils.class,
					URL.class, URI.class, HttpURLConnection.class, IOUtils.class, StandardCharsets.class};
			for (Class<?> clazz : classes)
				engine.eval("var " + clazz.getSimpleName() + " = Java.type('" + clazz.getName() + "');");
			engine.eval("function print(msg) {\n\tchannel.sendMessage(msg);\n}");
			engine.eval("function loadScript(path) {\n\tJSEvalCommand.loadScript(path);\n}");
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static Object loadScript(String path)
	{
		return loadScript(new File(path));
	}

	public static Object loadScript(File file)
	{
		try
		{
			return engine.eval(new FileReader(file));
		}
		catch (ScriptException | FileNotFoundException e)
		{
			return "Erreur : " + e.getMessage();
		}
	}

	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		if (engine.get("bot") == null)
			engine.put("bot", DiscordBot.getClient().getOurUser());

		engine.put("user", executor);
		engine.put("author", executor);
		engine.put("executor", executor);
		engine.put("channel", executedIn);
		engine.put("guild", executedIn.getGuild());

		new MessageBuilder(DiscordBot.getClient()).withChannel(executedIn).withContent("Exécution de :\n").appendCode("js", String.join(" ", args)).build();

		try
		{
			Object obj = engine.eval(String.join(" ", args));
			if (obj != null)
				executedIn.sendMessage(String.valueOf(obj));
		}
		catch (SecurityException ex)
		{
			executedIn.sendMessage("Erreur de sécurité : " + ex.getMessage());
		}
		catch (Exception ex)
		{
			executedIn.sendMessage("Erreur lors de l’exécution de « " + String.join(" ", args) + " » : " + ex.getMessage());
		}
	}

	@Override
	public String getName()
	{
		return "evaluate";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"eval", "javascript", "js"};
	}

	@Override
	public String getHelp()
	{
		return "";
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}
}
