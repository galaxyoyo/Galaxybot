package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class SayCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		executedIn.sendMessage(String.join(" ", args));
	}

	@Override
	public String getName()
	{
		return "say";
	}

	@Override
	public String[] getAliases()
	{
		return new String[0];
	}

	@Override
	public String getHelp()
	{
		return "Dit un message";
	}
}
