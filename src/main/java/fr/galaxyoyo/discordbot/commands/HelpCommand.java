package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class HelpCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		StringBuilder builder = new StringBuilder();
		builder.append("**Aide de Galaxybot :**\n``-----------------``\n");
		for (Command cmd : CommandListener.getAllCommands())
		{
			if (!cmd.isVisible())
				continue;

			String space = "";
			for (int i = 0; i < 16 - cmd.getName().length(); ++i)
			{
				if (space.isEmpty() && cmd.getName().length() % 2 == 1)
					space += " ";
				else
					space += "_";
			}
			builder.append("**``" + CommandListener.PREFIX).append(cmd.getName()).append(space).append("``** — ").append(cmd.getHelp());
			String[] aliases = cmd.getAliases();
			if (aliases != null && aliases.length > 0)
			{
				builder.append(", *aliases : ");
				for (int i = 0; i < aliases.length; ++i)
				{
					builder.append("``").append(CommandListener.PREFIX).append(aliases[i]).append("``");
					if (i + 1 < aliases.length)
						builder.append(", ");
				}
				builder.append("*");
			}
			builder.append("\n");
		}

		builder.append("``-----------------``");

		executedIn.sendMessage(builder.toString());
	}

	@Override
	public String getName()
	{
		return "help";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"h", "?"};
	}

	@Override
	public String getHelp()
	{
		return "Affiche ce message d'aide";
	}
}
