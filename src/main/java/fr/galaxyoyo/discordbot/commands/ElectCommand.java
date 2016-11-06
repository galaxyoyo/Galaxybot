package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import fr.galaxyoyo.discordbot.Survey;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ElectCommand implements Command
{
	private static Role MOW, COW, GMOW, SM;
	private final Map<RoleToElect, List<IUser>> usersByElection = new HashMap<>();

	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		if (MOW == null)
			MOW = (Role) executedIn.getGuild().getRoleByID("241236019964411904");
		if (COW == null)
			COW = (Role) executedIn.getGuild().getRoleByID("244167176578138113");
		if (GMOW == null)
			GMOW = (Role) executedIn.getGuild().getRoleByID("227502875821998080");

		if (SM == null)
			SM = (Role) executedIn.getGuild().getRoleByID("227479116461506564");

		if (!executedIn.getName().contains("election"))
		{
			executedIn.sendMessage("Vous ne pouvez élire quiconque ici.");
			return;
		}

		if (args.length != 1)
		{
			new MessageBuilder(DiscordBot.getClient()).withContent("Usage : ", MessageBuilder.Styles.BOLD).appendContent("/elect ").appendContent("[mow | cow | mgw]", MessageBuilder
					.Styles.ITALICS).withChannel(executedIn).build();
			return;
		}

		RoleToElect rte;

		try
		{
			rte = RoleToElect.valueOf(args[0].toUpperCase());
		}
		catch (IllegalArgumentException ex)
		{
			new MessageBuilder(DiscordBot.getClient()).withContent("Usage : ", MessageBuilder.Styles.BOLD).appendContent("/elect ").appendContent("[mow | cow | mgw]", MessageBuilder
					.Styles.ITALICS).withChannel(executedIn).build();
			return;
		}

		if (usersByElection.containsKey(rte))
		{
			executedIn.sendMessage(executor.mention() + " Une élection est déjà en cours.");
			return;
		}

		Survey survey = Survey.newSurvey(executor, executedIn);
		survey.setQuestion("Votez pour le " + rte.name() + " (" + rte.getDesc() + ")");
		survey.setType(Survey.Type.UNIQUE_CHOICE);
		survey.setReady();
		final List<IUser> users = new ArrayList<>(executedIn.getGuild().getUsers().stream().filter(iUser -> !iUser.isBot()).collect(Collectors.toList()));
		users.sort((user1, user2) ->
		{
			int i = user1.getPresence().compareTo(user2.getPresence());
			if (i != 0)
				return i;
			return String.CASE_INSENSITIVE_ORDER.compare(user1.getDisplayName(executedIn.getGuild()), user2.getDisplayName(executedIn.getGuild()));
		});
		for (IUser user : users)
			survey.addAnswer("@" + user.getDisplayName(executedIn.getGuild()));

		survey.addEndSurveyListener(s ->
		{
			Map.Entry<Integer, Integer> max = null;
			List<Integer> equalityIndexes = new ArrayList<>();
			for (Map.Entry<Integer, Integer> entry : s.getAnswerRatio().entrySet())
			{
				if (max == null || entry.getValue() > max.getValue())
				{
					max = entry;
					equalityIndexes.clear();
					equalityIndexes.add(entry.getKey());
				}
				else if (entry.getValue().intValue() == max.getValue().intValue())
					equalityIndexes.add(entry.getKey());
			}

			List<IUser> equalities = equalityIndexes.stream().map(users::get).collect(Collectors.toList());
			if (equalities.size() == 1)
				executedIn.sendMessage("@everyone Et le " + rte.name() + " est ... " + equalities.get(0).mention() + " !");
			else
			{
				executedIn.sendMessage("@everyone Nous avons droit cette semaine à " + equalities.size() + " " + rte.name() + ". Ceci est entièrement de votre faute. Ils sont : "
						+ equalities.stream().map(IUser::mention).collect(Collectors.toList()));
			}

			Role role = rte.getRole();
			role.getGuild().getUsers().forEach(iUser ->
			{
				try
				{
					if (iUser.getRolesForGuild(executedIn.getGuild()).contains(role))
					{
						iUser.removeRole(role);
						if (role == GMOW)
							iUser.addRole(SM);
						Thread.sleep(500L);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});

			for (IUser iUser : equalities)
			{
				iUser.addRole(role);
				if (role == GMOW)
					iUser.removeRole(SM);
				Thread.sleep(500L);
			}

			usersByElection.remove(rte);
		});
	}

	@Override
	public String getName()
	{
		return "elect";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"e"};
	}

	@Override
	public String getHelp()
	{
		return "Élire le MOW, le COW (vache) ou la GMW.";
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}

	@SuppressWarnings("unused")
	public enum RoleToElect
	{
		MOW("Mullet of the Week", () -> ElectCommand.MOW),
		COW("Calvitie of the Week", () -> ElectCommand.COW),
		GMOW("Grosse Merde of the Week", () -> ElectCommand.GMOW);

		private final String desc;
		private final Supplier<Role> role;

		RoleToElect(String desc, Supplier<Role> role)
		{
			this.desc = desc;
			this.role = role;
		}

		public String getDesc()
		{
			return desc;
		}

		public Role getRole()
		{
			return role.get();
		}
	}
}
