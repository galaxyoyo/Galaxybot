package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class DeleteMessageCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		executedIn.getGuild().getMessageByID(args[0]).delete();
	}

	@Override
	public String getName()
	{
		return "delete";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"d", "del", "remove", "rem", "rm"};
	}

	@Override
	public String getHelp()
	{
		return "Supprime un message";
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}
}
