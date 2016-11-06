package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import org.apache.commons.io.output.NullOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;

public class SexCommand implements Command
{
	@Override
	public void execute(User executor, Channel channel, String... args) throws Exception
	{
		String search = null;
		int amount = 1;

		if (!channel.isPrivate())
		{
			String cName = channel.getName().toLowerCase();

			if (!cName.contains("sex") && !cName.contains("18") && !cName.contains("censor") && !cName.contains("censure") && !cName.contains("zbeub") && !cName.contains("bite")
					&& !cName.contains("dick") && !cName.contains("dev") && !cName.contains("porn"))
			{

				new MessageBuilder(DiscordBot.getClient()).withContent("Le p0rn n’est pas autorisé dans ce canal !").withChannel(channel).build();
				return;
			}

			if (!DiscordBot.checkPermission(channel, Permissions.ATTACH_FILES))
			{
				new MessageBuilder(DiscordBot.getClient()).withContent("Le bot n’a pas la permission d’attacher des fichiers !", MessageBuilder.Styles.BOLD).withChannel(channel)
						.build();
				return;
			}
		}

	/*	if (executor.getID().equals("87279950075293696"))
		{
			try
			{
				URLConnection connection = new URL("http://humourtop.com/hommes-les-plus-moches-du-monde/Homme_monstrueux_humour.jpg").openConnection();
				InputStream img = connection.getInputStream();
				channel.sendFile(img, "sex-" + executor.getName() + ".png");
			}
			catch (IOException e)
			{
				DiscordBot.logger.log(Level.SEVERE, "Couldn't get image:", e);
			}

			return;
		}*/

		if (args.length >= 1)
		{
			try
			{
				int input = Integer.parseInt(args[0]);
				amount = Math.min(5, input);
			}
			catch (NumberFormatException ignored)
			{
			}

			if (args.length >= 2)
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 1; i < args.length; i++)
				{
					if (sb.length() != 0)
						sb.append(" ");

					sb.append(args[i]);
				}

				search = sb.toString();
			}
		}

		PrintStream oldErr = System.err;
		NullOutputStream nullOs = new NullOutputStream();
		System.setErr(new PrintStream(nullOs));
		try
		{
			int page = new Random().nextInt(20) + 1;

			InputStream input = new URL(search == null ? ("http://www.sex.com/?page=" + page) : ("http://www.sex.com/search/pictures?query=" + search)).openStream();
			Tidy tidy = new Tidy();
			Document document = tidy.parseDOM(input, null);
			NodeList imgs = document.getElementsByTagName("img");

			System.setErr(oldErr);

			for (int i = 0; i < amount; i++)
			{
				Node item;

				int tries = 0;

				do
				{
					int ri = new Random().nextInt(imgs.getLength());
					item = imgs.item(ri);

					tries++;
				} while ((!item.hasAttributes() || item.getAttributes().getNamedItem("data-src") == null)
						&& tries < 2);

				if (!item.hasAttributes() || item.getAttributes().getNamedItem("data-src") == null)
				{
					new MessageBuilder(DiscordBot.getClient()).withContent("Aucune image n’a été trouvée correspondant à votre demande.").withChannel(channel).build();
					return;
				}

				String url = (item.getAttributes().getNamedItem("data-src").getNodeValue().replace("/236/", "/620/"));
		/*		URLConnection connection = new URL(url).openConnection();

				InputStream img = connection.getInputStream();
				channel.sendFile(img, "sex-" + executor.getName() + url.substring(url.length() - 4));*/
				channel.sendMessage(url);
			}
		}
		catch (IOException e)
		{
			DiscordBot.logger.log(Level.SEVERE, "Couldn't get image:", e);
			System.setErr(oldErr);
		}

	}

	@Override
	public String getName()
	{
		return "sex";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"bite", "dick"};
	}

	@Override
	public String getHelp()
	{
		return "Affiche une image (18+)";
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}
}
