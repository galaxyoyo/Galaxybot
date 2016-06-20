package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.games.Minesweeper;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class MinesweeperCommand implements Command
{
	@Override
	public void execute(User exector, Channel executedIn, String... args) throws Exception
	{
		try
		{
			Minesweeper.launch(exector, executedIn);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public String getName()
	{
		return "minesweeper";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"ms", "démineur", "mine"};
	}

	@Override
	public String getHelp()
	{
		return "Lance une partie de démineur";
	}
}
