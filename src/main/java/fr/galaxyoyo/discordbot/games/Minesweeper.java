package fr.galaxyoyo.discordbot.games;

import fr.galaxyoyo.discordbot.DiscordBot;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Minesweeper
{
	private static final Random RANDOM = new Random();
	private static final int MAX_WIDTH = 18, MAX_HEIGHT = 18;
	private static Map<Channel, Minesweeper> parties = new HashMap<>();
	private final Channel channel;
	private final User player;
	private int width = -1;
	private int height = -1;
	private int mines = -1;
	private int wins = -1;
	private int[][] table;
	private boolean[][] mineTable;
	private Message msg;

	public Minesweeper(Channel channel, User player)
	{
		this.channel = channel;
		this.player = player;
	}

	public static void launch(User player, Channel channel) throws Exception
	{
		if (parties.containsKey(channel))
		{
			channel.sendMessage("Une partie de démineur est déjà en cours sur ce canal !");
			return;
		}

		Minesweeper ms = new Minesweeper(channel, player);
		parties.put(channel, ms);
		DiscordBot.getClient().getDispatcher().registerListener(ms);


		channel.sendMessage("Démarrage d'une partie de démineur par " + player.mention() + " !");
		channel.sendMessage("[Démineur] " + player.mention() + " Quelle doit être la largeur de la grille ? [MAX : " + MAX_WIDTH + "]");
	}

	public void stop()
	{
		try
		{
			channel.unpin(msg);
			parties.remove(channel);
			DiscordBot.getClient().getDispatcher().unregisterListener(this);
			channel.sendMessage("[Démineur] Fin de la partie.");
		}
		catch (MissingPermissionsException | DiscordException | RateLimitException ex)
		{
			ex.printStackTrace();
		}
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) throws Exception
	{
		if (event.getMessage().getChannel() != channel)
			return;

		if (!event.getMessage().getAuthor().isBot() && table != null)
			showTable();

		if (event.getMessage().getAuthor() != player)
			return;

		String content = event.getMessage().getContent().trim();
		if (content.startsWith("@"))
			return;
		event.getMessage().delete();

		if (content.equalsIgnoreCase("stop"))
		{
			stop();
			return;
		}

		if (width <= 0 || height <= 0 || mines <= 0)
		{
			boolean ok;
			int max = 0;
			try
			{
				int i = Integer.parseInt(content);
				ok = i > 0;
				if (ok)
				{
					if (width <= 0)
					{
						max = MAX_WIDTH;
						if (i > max)
							ok = false;
						else
							width = i;
						channel.sendMessage("[Démineur] " + player.mention() + " Quelle doit être la hauteur de la grille ? [MAX : " +
								Math.min(MAX_HEIGHT, 14 * 14 / width) + "]");
					}
					else if (height <= 0)
					{
						max = Math.min(MAX_HEIGHT, 14 * 14);
						if (i > max)
							ok = false;
						else
							height = i;
						channel.sendMessage("[Démineur] " + player.mention() + " Combien de mines dans la grille ? [MAX : " + (width * height - 1) + "]");
					}
					else
					{
						max = width * height - 1;
						if (i > max)
							ok = false;
						else
						{
							mines = i;
							wins = i;
						}
						channel.sendMessage("[Démineur] " + player.mention() + " C'est parti pour une grille de " + width + "x" + height + " avec " + mines + " mine" +
								(mines > 1 ? "s" : "") + " !");
						channel.sendMessage("[Démineur] " + player.mention() + " Pour jouer, tape les coordoonées de la case à déterrer (par exemple B;4) et rajoutant " +
								"F:" +
								" devant pour mettre un drapeau (par exemple, F:E;3)");
						table = new int[width][height];
						mineTable = new boolean[width][height];
						generate();
						showTable();
					}
				}
			}
			catch (NumberFormatException ex)
			{
				ok = false;
			}

			if (!ok)
				channel.sendMessage("[Démineur] " + player.mention() + " \"" + content + "\" n'est pas un nombre entier strictement positif inférieur à " + max + ".");
			return;
		}

		boolean flag = false;
		if (content.startsWith("F:"))
		{
			flag = true;
			content = content.substring(2).trim();
		}

		String[] split = content.split(";");
		if (split.length != 2)
		{
			channel.sendMessage("[Démineur] " + player.mention() + "  Les coordonnées doivent être sous la forme X;Y ou F:X;Y.");
			return;
		}

		if (split[0].length() != 1)
		{
			channel.sendMessage("[Démineur] " + player.mention() + "  L'abscisse ne doit comporter qu'une seule lettre.");
			return;
		}

		int x = split[0].charAt(0) - 'A';
		if (x < 0 || x >= width)
		{
			channel.sendMessage("[Démineur] " + player.mention() + "  L'abscisse doît être comprise entre A et " + (char) ('A' + width - 1) + ".");
			return;
		}

		int y;
		try
		{
			y = Integer.parseInt(split[1]) - 1;
			if (y < 0 || y >= height)
			{
				channel.sendMessage("[Démineur] " + player.mention() + "  L'ordonnée doît être comprise entre 1 et " + height + ".");
				return;
			}
		}
		catch (NumberFormatException ex)
		{
			channel.sendMessage("[Démineur] " + player.mention() + " \"" + split[1] + "\" n'est pas un nombre entier valide.");
			return;
		}

		if (flag)
		{
			if (table[x][y] == 10)
			{
				table[x][y] = 0;
				if (mineTable[x][y])
					++wins;
			}
			else if (table[x][y] == 0)
			{
				table[x][y] = 10;
				if (mineTable[x][y])
					--wins;

				if (wins == 0)
					win();
			}
		}
		else if (table[x][y] == 0)
			calculate(x, y);
		showTable();
	}

	private void generate()
	{
		for (int i = 0; i < mines; ++i)
		{
			int x, y;
			do
			{
				x = RANDOM.nextInt(width);
				y = RANDOM.nextInt(height);
			}
			while (mineTable[x][y]);

			mineTable[x][y] = true;
		}
	}

	private int calculate(int x, int y)
	{
		if (mineTable[x][y])
		{
			loose();
			return 1;
		}

		int sum = 0;
		for (int dx = -1; dx <= 1; ++dx)
		{
			for (int dy = -1; dy <= 1; ++dy)
			{
				if (dx == 0 && dy == 0 || x + dx < 0 || x + dx >= width || y + dy < 0 || y + dy >= height)
					continue;
				if (mineTable[x + dx][y + dy])
					++sum;
			}
		}

		int c = sum;
		if (c == 0)
			c = 11;
		table[x][y] = c;
		if (c == 11)
		{
			for (int dx = -1; dx <= 1; ++dx)
			{
				for (int dy = -1; dy <= 1; ++dy)
				{
					if (dx == 0 && dy == 0 || x + dx < 0 || x + dx >= width || y + dy < 0 || y + dy >= height || table[x + dx][y + dy] != 0)
						continue;
					calculate(x + dx, y + dy);
				}
			}
		}

		return sum;
	}

	private void win()
	{
		try
		{
			channel.sendMessage("[Démineur] " + player.mention() + "  Félécitations, vous avez gagné !");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		stop();
	}

	private void loose()
	{
		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				if (!mineTable[x][y])
					continue;
				table[x][y] = -1;
			}
		}
		stop();
	}

	public void showTable() throws Exception
	{
		StringBuilder content = new StringBuilder("```     ");
		for (int i = 0; i < width; ++i)
		{
			content.append((char) ('A' + i));
			if (i + 1 < width)
				content.append("   ");
		}
		content.append("\n   ");
		for (int i = 0; i <= width; ++i)
		{
			content.append("+");
			if (i < width)
				content.append("---");
		}
		content.append('\n');

		for (int y = 0; y < height; ++y)
		{
			content.append(y + 1);
			int space = 2;
			while (y + 1 >= Math.pow(10, 3 - space))
				--space;
			for (int i = 0; i < space; ++i)
				content.append(" ");
			content.append("|");
			for (int x = 0; x < width; ++x)
			{
				int c = table[x][y];
				char character;
				if (c == 0)
					character = '█';
				else if (c == 10)
					character = '¶';
				else if (c == 11)
					character = ' ';
				else if (c == -1)
					character = '☼';
				else
					character = Integer.toString(c).charAt(0);
				content.append(' ').append(character).append(" |");
			}
			content.append("\n   +");
			for (int x = 0; x < width; ++x)
				content.append("---+");
			content.append('\n');
		}

		content.append("```");

		if (msg != null)
			msg.edit(content.toString());
		else
		{
			msg = (Message) channel.sendMessage(content.toString());
			channel.pin(msg);
		}
	}
}
