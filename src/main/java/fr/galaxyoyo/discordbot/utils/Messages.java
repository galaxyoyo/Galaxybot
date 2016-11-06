package fr.galaxyoyo.discordbot.utils;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.impl.obj.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages
{
	public static List<User> getUserMentions(Guild guild, String msg)
	{

		Pattern pattern = Pattern.compile("<(@|@!)(\\d+)>");

		Matcher matcher = pattern.matcher(msg);


		List<User> result = new ArrayList<>();


		while (matcher.find())
		{

			String id = matcher.group(2);

			User user = (User) guild.getUserByID(id);


			if (user != null)

				result.add(user);

		}


		return result;

	}


	public static String replaceMentions(Guild guild, String msg)
	{

		Pattern pattern = Pattern.compile("<(@|@!|@&|#)(\\d+)>");

		Matcher matcher = pattern.matcher(msg);


		StringBuffer result = new StringBuffer();


		while (matcher.find())
		{

			boolean channelMention = matcher.group(1).equals("#");

			String id = matcher.group(2);


			if (!channelMention)
			{

				User user = (User) guild.getUserByID(id);


				if (user != null)
				{

					String name = user.getDisplayName(guild);

					matcher.appendReplacement(result, "@" + name);


					continue;

				}


				Role role = (Role) guild.getRoleByID(id);


				if (role != null)

					matcher.appendReplacement(result, "@" + role.getName());

			}

			else
			{

				Channel channel = (Channel) guild.getChannelByID(id);


				if (channel != null)

					matcher.appendReplacement(result, "#" + channel.getName());

			}

		}


		matcher.appendTail(result);

		return result.toString();

	}
}
