package fr.galaxyoyo.discordbot;

import fr.galaxyoyo.discordbot.utils.Messages;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Survey
{
	private static Map<Channel, Survey> surveys = new HashMap<>();
	private final Channel channel;
	private final User owner;
	private List<User> answered = new ArrayList<>();
	private Map<Integer, Integer> answerRatio = new HashMap<>();
	private Date endDate;
	private Type type;
	private String question;
	private List<String> answers = new ArrayList<>();
	private Message surveyMessage;
	private boolean ready = false;
	private List<EndSurveyListener> endListeners = new ArrayList<>();

	public Survey(Channel channel, User owner)
	{
		this.channel = channel;
		this.owner = owner;
	}

	@SuppressWarnings("unused")
	public static Survey getSurvey(Channel channel)
	{
		return surveys.get(channel);
	}

	public static Survey newSurvey(User owner, Channel channel) throws Exception
	{
		if (surveys.containsKey(channel))
		{
			channel.sendMessage("Un sondage est déjà en cours sur ce canal !");
			return surveys.get(channel);
		}

		Survey survey = new Survey(channel, owner);
		surveys.put(channel, survey);
		DiscordBot.getClient().getDispatcher().registerListener(survey);

		channel.sendMessage(owner.mention() + " [Sondage] Combien de temps doit rester le sondage ? (en secondes)");

		return survey;
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

		String content = Messages.replaceMentions((Guild) event.getMessage().getGuild(), event.getMessage().getContent().trim());
		if (content.startsWith("!"))
			return;

		if (endDate == null)
		{
			event.getMessage().delete();
			try
			{
				long seconds = Long.parseLong(content);
				if (seconds <= 0)
					channel.sendMessage(owner.mention() + " [Sondage] '" + content + "' n’est pas un nombre entier positif valide.");
				else
				{
					setEndDate(new Date(seconds * 1000L));
					if (ready)
						start();
					else
						channel.sendMessage(owner.mention() + " [Sondage] De quel type est votre question ? (1 = Choix multiples, 2 = Choix unique)");
				}
			}
			catch (NumberFormatException ex)
			{
				channel.sendMessage(owner.mention() + " [Sondage] '" + content + "' n’est pas un nombre entier positif valide.");
			}
			return;
		}

		if (type == null)
		{
			event.getMessage().delete();
			int typeId;
			try
			{
				typeId = Integer.parseInt(content);
				if (typeId != 1 && typeId != 2)
					channel.sendMessage(owner.mention() + " [Sondage] '" + content + "' n’est pas un nombre entier positif valide.");
				else
				{
					setType(Type.values()[typeId - 1]);
					channel.sendMessage(owner.mention() + " [Sondage] Quelle est votre question ?");
				}
			}
			catch (NumberFormatException ex)
			{
				channel.sendMessage(owner.mention() + " [Sondage] '" + content + "' n’est pas un nombre entier positif valide.");
			}
			return;
		}

		if (question == null)
		{
			event.getMessage().delete();
			setQuestion(content);
			channel.sendMessage(owner.mention() + " [Sondage] Veuillez maintenant entrer vos réponses. Tapez ``end`` lorsque vous avez terminé.");
			return;
		}

		if (!ready)
		{
			event.getMessage().delete();
			if (content.equalsIgnoreCase("end"))
			{
				start();
				channel.sendMessage("[Sondage] Sondage prêt !");
			}
			else
				addAnswer(content);
			return;
		}

		//noinspection SuspiciousMethodCalls
		if (answered.contains(event.getMessage().getAuthor()))
			return;

		event.getMessage().delete();

		if (type == Type.UNIQUE_CHOICE)
		{
			int answerId;
			try
			{
				answerId = Integer.parseInt(content);
				if (answerId <= 0 || answerId > answers.size())
				{
					channel.sendMessage(event.getMessage().getAuthor().mention() + "Votre réponse doit se situer entre 1 et " + answers.size() + ".");
					return;
				}
			}
			catch (NumberFormatException ex)
			{
				channel.sendMessage(event.getMessage().getAuthor().mention() + " '" + content + "' n’est pas un entier naturel valide.");
				return;
			}
			answered.add((User) event.getMessage().getAuthor());
			answerRatio.put(answerId - 1, answerRatio.get(answerId - 1) + 1);
			updateSurvey();
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
						channel.sendMessage(event.getMessage().getAuthor().mention() + "Votre réponse doit se situer entre 1 et " + answers.size() + ".");
						return;
					}
					answerIds.add(answerId);
				}
				catch (NumberFormatException ex)
				{
					channel.sendMessage(event.getMessage().getAuthor().mention() + " '" + content + "' n’est pas un entier naturel valide.");
					return;
				}
			}
			answered.add((User) event.getMessage().getAuthor());
			for (int answerId : answerIds)
				answerRatio.put(answerId - 1, answerRatio.get(answerId - 1) + 1);
			updateSurvey();
		}

		channel.sendMessage(event.getMessage().getAuthor().mention() + " a voté !");
	}

	public void start()
	{
		setReady();
		setEndDate(new Date(getEndDate().getTime() + System.currentTimeMillis()));
		try
		{

			updateSurvey();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		new Thread(() ->
		{

			while (true)
			{
				try
				{
					Thread.sleep(1000L);

					if (System.currentTimeMillis() > endDate.getTime())
					{
						end();
						break;
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public void setQuestion(String question)
	{
		this.question = question;
	}

	public void addAnswer(String answer)
	{
		answers.add(answer);
		answerRatio.put(answers.size() - 1, 0);
		//	channel.sendMessage(owner.mention() + " [Sondage] Réponse ``" + content + "`` ajoutée.");
	}

	public void updateSurvey() throws Exception
	{
		String date = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm:ss.S Z").format(endDate);
		String text;
		if (type == Type.MULTIPLE_CHOICES)
			text = "Tapez vos numéros de réponse séparés par un espace. (Plusieurs choix possibles). Fin : " + date;
		else
			text = "Tapez votre numéro de réponse. (Un choix possible). Fin : " + date;
		text += "\n";
		text += "```\n" + question + "\n";
		AtomicInteger maxLength = new AtomicInteger(-1);
		answers.stream().filter(answer -> answer.length() > maxLength.get()).forEach(answer -> maxLength.set(answer.length()));
		for (int i = 0; i < answers.size(); ++i)
		{
			text += (i + 1) + ") " + answers.get(i);
			for (int j = 0; j < maxLength.get() - answers.get(i).length() + 4 - (int) Math.floor(Math.log10(i + 1)); j++)
				text += " ";
			text += answerRatio.get(i) + " (" + (Math.round(10000.0D * answerRatio.get(i) / (double) answered.size()) / 100D) + " %)\n";
		}
		text += "```";
		if (surveyMessage != null)
			surveyMessage.edit(text);
		else
		{
			surveyMessage = (Message) channel.sendMessage(text);
			channel.pin(surveyMessage);
		}
	}

	public void setReady()
	{
		ready = true;
	}

	@SuppressWarnings("unused")
	public Date getEndDate()
	{
		return endDate;
	}

	public void end()
	{
		try
		{
			endListeners.forEach(listener ->
			{
				try
				{
					listener.onSurveyEnded(this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			if (surveyMessage.isPinned())
				channel.unpin(surveyMessage);
			surveys.remove(channel);
			DiscordBot.getClient().getDispatcher().unregisterListener(this);
			channel.sendMessage("[Sondage] Sondage terminé.");
		}
		catch (MissingPermissionsException | DiscordException | RateLimitException ex)
		{
			ex.printStackTrace();
		}
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public void addEndSurveyListener(EndSurveyListener listener)
	{
		endListeners.add(listener);
	}

	@SuppressWarnings("unused")
	public List<User> getAnswered()
	{
		return answered;
	}

	@SuppressWarnings("unused")
	public List<String> getAnswers()
	{
		return answers;
	}

	public Map<Integer, Integer> getAnswerRatio()
	{
		return answerRatio;
	}

	public enum Type
	{
		MULTIPLE_CHOICES, UNIQUE_CHOICE
	}

	@FunctionalInterface
	public interface EndSurveyListener
	{
		void onSurveyEnded(Survey survey) throws Exception;
	}
}
