package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.Survey;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class SurveyCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		Survey.newSurvey(executor, executedIn);
	}

	@Override
	public String getName()
	{
		return "survey";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"s", "sondage", "sond", "surv"};
	}

	@Override
	public String getHelp()
	{
		return "Poste un sondage.";
	}
}
