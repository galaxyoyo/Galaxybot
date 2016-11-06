package fr.galaxyoyo.discordbot.utils;

import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;

public class Users
{

	public static User search(Guild guild, String name)
	{
		if (!guild.getUsersByName(name).isEmpty())
			return (User) guild.getUsersByName(name).get(0);

		User bestUser = null;
		double bestScore = -1;

		for (IUser iuser : guild.getUsers())
		{
			User user = (User) iuser;
			double score = StringUtils.getJaroWinklerDistance(user.getDisplayName(guild), name);

			if (score <= 0.75)
				continue;

			String nick = user.getNicknameForGuild(guild).orElse(null);

			if (nick != null)
			{
				double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

				if (nickScore > score)
					score = nickScore;
			}

			if (BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore)
			{
				bestUser = user;
				bestScore = score;
			}
		}

		return bestUser;
	}

	public static User search(Guild guild, String name, String discriminator)
	{
		for (IUser iuser : guild.getUsersByName(name))
		{
			User user = (User) iuser;
			if (user.getDiscriminator().equals(discriminator))
				return user;
		}

		User bestUser = null;
		double bestScore = -1;

		for (IUser iuser : guild.getUsers())
		{
			User user = (User) iuser;
			if (!user.getDiscriminator().equals(discriminator))
				continue;

			double score = StringUtils.getJaroWinklerDistance(user.getDisplayName(guild), name);
			String nick = user.getNicknameForGuild(guild).orElse(null);

			if (nick != null)
			{
				double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

				if (nickScore > score)
					score = nickScore;
			}

			if (BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore)
			{
				bestUser = user;
				bestScore = score;
			}
		}

		return bestUser;
	}
}
