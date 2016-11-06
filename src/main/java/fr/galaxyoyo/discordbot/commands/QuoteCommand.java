package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import fr.galaxyoyo.discordbot.utils.Quote;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class QuoteCommand implements Command
{
	public void execute(User executor, Channel executedIn, String[] args) throws Exception
	{
		if (args.length < 1)
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("Syntaxe incorrecte ! ", MessageBuilder.Styles.BOLD).appendContent(CommandListener.PREFIX
					+ "quote <message à trouver>").withChannel(executedIn).build();
			return;

		}

		if (executedIn.isPrivate())
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("Vous ne pouvez faire de citations dans des canaux privés.").withChannel(executedIn).build();
			return;
		}

		if (!DiscordBot.checkPermission(executedIn, Permissions.ATTACH_FILES))
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("Le bot n’a pas la permission pour envoyer des fichiers !", MessageBuilder.Styles.BOLD)
					.withChannel(executedIn).build();
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (String arg : args)
		{
			if (sb.length() > 0)
				sb.append(" ");

			sb.append(arg.replace("\n", " ").replace("\r", " "));
		}

		List<IMessage> msgs = executedIn.getMessages();
		msgs.sort(Comparator.comparing(IDiscordObject::getCreationDate));
		msgs.remove(0);
		Optional<IMessage> opMsg = msgs.stream().filter(message -> message.getContent() != null && message.getContent().toLowerCase().replace("\n", " ")
				.replace("\r", " ").trim().contains(sb.toString().toLowerCase().trim())).findFirst();

		if (opMsg.isPresent())
		{
			Message message = (Message) opMsg.get();
			LocalDateTime ldt = message.getCreationDate();
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			Quote quote = new Quote(executedIn, (User) message.getAuthor(), message.getContent(), date);
			quote.sendImage((User) message.getAuthor());
		}
		else
		{
			new MessageBuilder(DiscordBot.getClient()).appendContent("Aucun message n’a été trouvé ...").withChannel(executedIn).build();
		}
	}

	@Override
	public String getName()
	{
		return "quote";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"q"};
	}

	@Override
	public String getHelp()
	{
		return "Cite le message de quelqu’un dans ce canal.";
	}
}