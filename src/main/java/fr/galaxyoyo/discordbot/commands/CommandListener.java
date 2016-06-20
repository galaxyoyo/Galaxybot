package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;

import java.util.*;

public class CommandListener implements IListener<MessageReceivedEvent>
{
	public static final char PREFIX = '&';
	private static Map<String, Command> commands = new TreeMap<>(String::compareTo);

	static
	{
		registerCommand(new HelpCommand());
		registerCommand(new MinesweeperCommand());
		registerCommand(new SurveyCommand());
		registerCommand(new LatexCommand());
	}

	public static void registerCommand(Command cmd)
	{
		commands.put(cmd.getName(), cmd);
		for (String alias : cmd.getAliases())
			commands.put(alias, cmd);
	}

	public static Collection<Command> getAllCommands()
	{
		return new HashSet<>(commands.values());
	}

	@Override
	public void handle(MessageReceivedEvent event)
	{
		try
		{
			Message msg = (Message) event.getMessage();
			String content = msg.getContent();
			if (content.isEmpty())
				return;

			if (content.charAt(0) == PREFIX)
			{
				msg.delete();
				String[] split = content.substring(1).split(" ");
				String alias = split[0];
				Command cmd = commands.get(alias.toLowerCase());
				if (cmd == null)
				{
					msg.reply("Impossible d'évaluer la commande : “" + content.substring(1) + "”. Faîtes " + PREFIX + "help pour afficher toutes les commandes disponibles.");
					return;
				}

				String[] args = new String[0];
				if (split.length > 1)
					args = Arrays.copyOfRange(split, 1, split.length);

				cmd.execute((User) msg.getAuthor(), (Channel) msg.getChannel(), args);
			}
		}
		catch (Exception ex)
		{
			DiscordBot.sendTemporaryMessage("Erreur lors de l'analyse de la commande : " + ex.getMessage(), 60000L);
			ex.printStackTrace();
		}
	}
}
