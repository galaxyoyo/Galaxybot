package fr.galaxyoyo.discordbot.utils;

import fr.galaxyoyo.discordbot.DiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IRole;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Quote
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Quote.class);
	private static Font font;
	private static Font boldFont;

	static
	{
		try
		{
			font = Font.createFont(Font.TRUETYPE_FONT, DiscordBot.class.getResourceAsStream("/fonts/WhitneyMedium.ttf"));
			boldFont = Font.createFont(Font.TRUETYPE_FONT, DiscordBot.class.getResourceAsStream("/fonts/WhitneySemibold.ttf"));
		}
		catch (FontFormatException | IOException e)
		{
			font = new Font("Arial", Font.PLAIN, 20);
			boldFont = new Font("Arial", Font.BOLD, 20);
			LOGGER.error("Couldn't init fonts:", e);
		}
	}

	private Channel channel;
	private User author;
	private String msg;
	private Date date;

	public Quote(Channel channel, User author, String msg, Date date)
	{
		this.channel = channel;
		this.author = author;
		this.msg = Messages.replaceMentions((Guild) channel.getGuild(), msg);
		this.date = date;
	}

	public void sendImage(@SuppressWarnings("unused") User asker)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH);
		BufferedImage img = new BufferedImage(1, 1, TYPE_INT_ARGB);
		List<String> lines = splitString(img.createGraphics().getFontMetrics(font.deriveFont(15f)), msg);

		if (lines.isEmpty())
			lines.add("");

		BufferedImage image = new BufferedImage(350, 35 + lines.size() * 15, TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		// Draw Profile Pic
		g2d.setComposite(AlphaComposite.Src);
		g2d.setColor(Color.WHITE);
		String defaultAvatarURL = getClass().getResource("/default-avatars/0.jpg").toString();
		defaultAvatarURL = defaultAvatarURL.substring(0, defaultAvatarURL.length() - 5) + Integer.parseInt(author.getDiscriminator()) % 5 + ".";
		switch (Integer.parseInt(author.getDiscriminator()) % 5)
		{
			case 1:
			case 2:
				defaultAvatarURL += "png";
				break;
			default:
				defaultAvatarURL += "jpg";
				break;
		}
		g2d.fill(new RoundRectangle2D.Float(0, 0, 50, 50, 50, 50));

		try
		{
			URL url = new URL(author.getAvatar() != null ? author.getAvatarURL() : defaultAvatarURL);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.connect();

			Image avatar = ImageIO.read(connection.getInputStream());
			g2d.setComposite(AlphaComposite.SrcAtop);
			g2d.drawImage(avatar, 0, 0, 50, 50, null);
		}
		catch (IOException e)
		{
			LOGGER.error("Couldn't get user avatar:", e);
		}

		g2d.setComposite(AlphaComposite.SrcOver);

		// Draw Name
		List<IRole> roles = author.getRolesForGuild(channel.getGuild());
		Role role = null;

		for (IRole ir : roles)
		{
			Role r = (Role) ir;
			if ((r.getColor().getRGB() & 0xFFFFFF) == 0)
				continue;

			if (role == null || role.getPosition() < r.getPosition())
				role = r;
		}

		if (role == null)
			g2d.setColor(new Color(220, 220, 220));
		else
			g2d.setColor(role.getColor());

		System.out.println(role);

		String displayName = author.getDisplayName(channel.getGuild());
		System.out.println(author.getDisplayName(channel.getGuild()));
		System.out.println(author.getNicknameForGuild(channel.getGuild()));
		System.out.println(author.getName());
		g2d.setFont(boldFont.deriveFont(16f));
		g2d.drawString(displayName, 70, 20);
		int nameWidth = g2d.getFontMetrics().stringWidth(displayName);

		// Draw Date
		g2d.setColor(new Color(100, 100, 100));
		g2d.setFont(font.deriveFont(10f));
		g2d.drawString(dateFormat.format(date), 78 + nameWidth, 20);

		// Draw Message
		g2d.setColor(new Color(160, 160, 160));
		g2d.setFont(font.deriveFont(15f));

		int y = 40;

		for (String line : lines)
		{
			g2d.drawString(line, 70, y);
			y += 15;
		}

		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			channel.sendFile(new ByteArrayInputStream(baos.toByteArray()), "quote-" + author.getDisplayName(channel.getGuild()) + ".png", author.mention(false));
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't send quote:", e);
		}
	}

	private List<String> splitString(FontMetrics metrics, String txt)
	{
		int maxWidth = 275;

		List<String> result = new ArrayList<>();
		String[] lines = txt.split("\n");

		for (String line : lines)
		{
			String[] words = line.split(" ");
			String currentLine = words[0];

			for (int i = 1; i < words.length; i++)
			{
				if (words[i].contains("\n") || words[i].contains("\r"))
				{
					result.add(currentLine);
					currentLine = words[i];
					continue;
				}

				if (metrics.stringWidth(currentLine + words[i]) < maxWidth)
					currentLine += " " + words[i];
				else
				{
					result.add(currentLine);
					currentLine = words[i];
				}
			}

			if (currentLine.trim().length() > 0)
				result.add(currentLine);
		}

		return result;
	}
}
