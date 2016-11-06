package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import fr.galaxyoyo.discordbot.utils.Messages;
import fr.galaxyoyo.discordbot.utils.Quote;
import fr.galaxyoyo.discordbot.utils.Users;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class FakeQuoteCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		if (args.length < 1)
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("Wrong syntax! ", MessageBuilder.Styles.BOLD).appendContent("$fakequote <user [#0123]> <message>")
					.withChannel(executedIn).build();
			return;
		}

		if (executedIn.isPrivate())
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("You can't make quotes in private channels.").withChannel(executedIn).build();
			return;
		}

		if (!DiscordBot.checkPermission(executedIn, Permissions.ATTACH_FILES))
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("No permission to send files!", MessageBuilder.Styles.BOLD).withChannel(executedIn).build();
			return;
		}

		User user = null;

		List<User> mentions = Messages.getUserMentions((Guild) executedIn.getGuild(), args[0]);

		if (mentions.size() == 1)
			user = mentions.get(0);

		if (user == null)
		{
			if (args[0].contains("#"))
				user = Users.search((Guild) executedIn.getGuild(), args[0].split("#")[0], args[0].split("#")[1]);
			else
				user = Users.search((Guild) executedIn.getGuild(), args[0]);
		}

		if (user != null)
		{
			String message = "";

			for (int i = 1; i < args.length; i++)
				message += args[i] + " ";

			Quote quote = new Quote(executedIn, user, message, new Date(System.currentTimeMillis() - new Random().nextInt(432_000_000)));
			quote.sendImage(executor);
		}
		else
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("User not found!").withChannel(executedIn).build();
		}

	}

	@Override
	public String getName()
	{
		return "fakequote";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"fquote", "fq"};
	}

	@Override
	public String getHelp()
	{
		return "Create a fake quote with a given player and message.";
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}
}
