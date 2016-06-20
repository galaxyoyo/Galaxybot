package fr.galaxyoyo.discordbot;

import sx.blah.discord.api.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Survey
{
	private static Map<Channel, Survey> surveys = new HashMap<>();
	private final Channel channel;
	private final User owner;
	private List<User> answered = new ArrayList<>();
	private Map<Integer, Integer> answerRatio = new HashMap<>();
	private int seconds = -1;
	private Type type;
	private String question;
	private List<String> answers = new ArrayList<>();
	private Message surveyMessage;
	private boolean ready = false;

	public Survey(Channel channel, User owner)
	{
		this.channel = channel;
		this.owner = owner;
	}

	public static void newSurvey(User owner, Channel channel) throws Exception
	{
		if (surveys.containsKey(channel))
		{
			DiscordBot.sendMessage(channel, "Un sondage est déjà en cours sur ce canal !");
			return;
		}

		Survey survey = new Survey(channel, owner);
		surveys.put(channel, survey);
		DiscordBot.getClient().getDispatcher().registerListener(survey);

		DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] Combien de temps doit rester le sondage ? (en secondes)", owner.getID());
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) throws Exception
	{
		if (event.getMessage().getChannel() != channel)
			return;

		if (!ready && event.getMessage().getAuthor() != owner)
			return;
		else if (!ready)
			event.getMessage().delete();

		String content = event.getMessage().getContent().trim();
		if (content.startsWith("@"))
			return;

		if (seconds <= 0)
		{
			try
			{
				seconds = Integer.parseInt(content);
				if (seconds <= 0)
					DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] '" + content + "' n'est pas un nombre entier positif valide.", owner.getID());
				else
					DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] De quel type est votre question ? (1 = Choix multiples, 2 = Choix unique)", owner.getID());
			}
			catch (NumberFormatException ex)
			{
				DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] '" + content + "' n'est pas un nombre entier positif valide.", owner.getID());
			}
			return;
		}

		if (type == null)
		{
			int typeId;
			try
			{
				typeId = Integer.parseInt(content);
				if (typeId != 1 && typeId != 2)
					DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] '" + content + "' n'est pas un nombre entier positif valide.", owner.getID());
				else
				{
					type = Type.values()[typeId - 1];
					DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] Quelle est votre question ?", owner.getID());
				}
			}
			catch (NumberFormatException ex)
			{
				DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] '" + content + "' n'est pas un nombre entier positif valide.", owner.getID());
			}
			return;
		}

		if (question == null)
		{
			question = content;
			DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] Veuillez maintenant entrer vos réponses. Tapez ``end`` lorsque vous avez terminé.", owner.getID());
			return;
		}

		if (!ready)
		{
			if (content.equalsIgnoreCase("end"))
			{
				ready = true;
				DiscordBot.sendMessage(channel, "[Sondage] Sondage prêt !");
				showSurvey();

				new Thread(() -> {
					try
					{
						Thread.sleep(seconds * 1000L);
						end();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}).start();
			}
			else
			{
				answers.add(content);
				answerRatio.put(answers.size() - 1, 0);
				DiscordBot.sendMessage(channel, "@" + owner.getName() + " [Sondage] Réponse ``" + content + "`` ajoutée.", owner.getID());
			}
			return;
		}

		//noinspection SuspiciousMethodCalls
		if (answered.contains(event.getMessage().getAuthor()))
		{
			showSurvey();
			return;
		}

		if (type == Type.UNIQUE_CHOICE)
		{
			int answerId;
			try
			{
				answerId = Integer.parseInt(content);
				if (answerId <= 0 || answerId > answers.size())
				{
					DiscordBot.sendMessage(channel, "@" + event.getMessage().getAuthor().getName() + "Votre réponse doit se situer entre 1 et " + answers.size() + ".", event
							.getMessage().getAuthor().getID());
					return;
				}
			}
			catch (NumberFormatException ex)
			{
				DiscordBot.sendMessage(channel, "@" + event.getMessage().getAuthor().getName() + " '" + content + "' n'est pas un nombre entier valide.", event.getMessage().getAuthor()
						.getID());
				return;
			}
			answered.add((User) event.getMessage().getAuthor());
			answerRatio.put(answerId - 1, answerRatio.get(answerId - 1) + 1);
			showSurvey();
		}
		else
		{
			String[] split = content.split(" ");
			Set<Integer> answerIds = new HashSet<>();
			for (String aSplit : split)
			{
				try
				{
					int answerId = Integer.parseInt(aSplit);
					if (answerId <= 0 || answerId > answers.size())
					{
						DiscordBot.sendMessage(channel, "@" + event.getMessage().getAuthor().getName() + "Votre réponse doit se situer entre 1 et " + answers.size() + ".", event
								.getMessage().getAuthor().getID());
						return;
					}
					answerIds.add(answerId);
				}
				catch (NumberFormatException ex)
				{
					DiscordBot.sendMessage(channel, "@" + event.getMessage().getAuthor().getName() + " '" + content + "' n'est pas un nombre entier valide.",
							event.getMessage().getAuthor()
									.getID());
					return;
				}
			}
			answered.add((User) event.getMessage().getAuthor());
			for (int answerId : answerIds)
				answerRatio.put(answerId - 1, answerRatio.get(answerId - 1) + 1);
			showSurvey();
		}
	}

	public void showSurvey() throws Exception
	{
		if (surveyMessage != null)
			surveyMessage.delete();
		String text;
		if (type == Type.MULTIPLE_CHOICES)
			text = "Tapez vos numéros de réponse séparés par un espace. (Plusieurs choix possibles)";
		else
			text = "Tapez votre numéro de réponse. (Un choix possible)";
		text += "\n";
		text += "```" + question + "\n";
		AtomicInteger maxLength = new AtomicInteger(-1);
		answers.stream().filter(answer -> answer.length() > maxLength.get()).forEach(answer -> maxLength.set(answer.length()));
		for (int i = 0; i < answers.size(); ++i)
		{
			text += (i + 1) + ") " + answers.get(i);
			for (int j = 0; j < maxLength.get() - answers.get(i).length() + 4; j++)
				text += " ";
			text += answerRatio.get(i) + " (" + (100 * answerRatio.get(i) / (double) answered.size()) + " %)\n";
		}
		text += "```";
		surveyMessage = DiscordBot.sendMessage(channel, text);
	}

	private void end()
	{
		try
		{
			surveys.remove(channel);
			DiscordBot.getClient().getDispatcher().unregisterListener(this);
			DiscordBot.sendMessage(channel, "[Sondage] Sondage terminé.");
		}
		catch (MissingPermissionsException | HTTP429Exception | DiscordException ex)
		{
			ex.printStackTrace();
		}
	}

	public enum Type
	{
		MULTIPLE_CHOICES, UNIQUE_CHOICE
	}
}
