package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.Permissions;

import java.util.*;

public class CommandListener implements IListener<MessageReceivedEvent>
{
	public static final char PREFIX = '§';
	private static Map<String, Command> commands = new TreeMap<>(String::compareTo);

	static
	{
		registerCommand(new HelpCommand());
		registerCommand(new MinesweeperCommand());
		registerCommand(new SayCommand());
		registerCommand(new TTSCommand());
		registerCommand(new QuoteCommand());
		registerCommand(new FakeQuoteCommand());
		registerCommand(new DeleteMessageCommand());
		registerCommand(new SurveyCommand());
		registerCommand(new LatexCommand());
		registerCommand(new SexCommand());
		registerCommand(new MulletCommand());
		registerCommand(new ElectCommand());
		registerCommand(new JSEvalCommand());
		registerCommand(new PythonEvalCommand());
		//	registerCommand(new FindRootsCommand());
	}

	public static void registerCommand(Command cmd)
	{
		commands.put(cmd.getName(), cmd);
		for (String alias : cmd.getAliases())
			commands.put(alias, cmd);
	}

	public static Collection<Command> getAllCommands()
	{
		Set<Command> set = new HashSet<>(commands.values());
		List<Command> l = new ArrayList<>(set);
		l.sort(Comparator.comparing(Command::getName));
		return l;
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
				DiscordBot.logger.info("@" + msg.getAuthor().getDisplayName(msg.getGuild()) + " issued command: " + content.substring(1));

				if (DiscordBot.checkPermission((Channel) msg.getChannel(), Permissions.MANAGE_MESSAGES))
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
			DiscordBot.sendTemporaryMessage("Erreur lors de l'analyse de la commande : " + ex.getClass().getSimpleName() + " : " + ex.getMessage(), (Channel) event.getMessage()
					.getChannel(), 60000L);
			ex.printStackTrace();
		}
	}
}
