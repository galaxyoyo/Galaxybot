package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class TTSCommand extends SayCommand
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		executedIn.sendMessage(String.join(" ", args), true);
	}

	@Override
	public String getName()
	{
		return "tts";
	}

	@Override
	public String getHelp()
	{
		return "Dit un message (TTS)";
	}
}
