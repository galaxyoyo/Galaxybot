package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public interface Command
{
	void execute(User executor, Channel executedIn, String... args) throws Exception;

	String getName();

	String[] getAliases();

	String getHelp();
}
