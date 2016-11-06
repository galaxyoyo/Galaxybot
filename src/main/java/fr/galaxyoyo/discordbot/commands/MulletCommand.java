package fr.galaxyoyo.discordbot.commands;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.Random;

public class MulletCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		User user = executor;
		if (args.length >= 1)
		{
			List<IUser> list = executedIn.getGuild().getUsersByName(args[0]);
			if (!list.isEmpty())
				user = (User) list.get(new Random().nextInt(list.size()));
		}

		if (new Random().nextBoolean())
			executedIn.sendMessage(user.mention() + " a un bon mulet", true);
		else
			executedIn.sendMessage(user.mention() + " n'a pas un bon mulet", true);
	}

	@Override
	public String getName()
	{
		return "mullet";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"mulet"};
	}

	@Override
	public String getHelp()
	{
		return "Affiche une image de mulet";
	}
}
