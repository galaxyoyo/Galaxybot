package fr.galaxyoyo.discordbot.commands;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class LatexCommand implements Command
{
	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{
		String str = StringUtils.join(args, " ");
		String code = DigestUtils.sha1Hex(str.getBytes(StandardCharsets.UTF_8));
		File dir = new File("latex");
		dir.mkdir();
		File file = new File(dir, code + ".png");
		if (!file.exists())
		{
			TeXFormula formula = new TeXFormula(str);
			TeXIcon icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(20).build();
			icon.setInsets(new Insets(5, 5, 5, 5));

			BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			JLabel label = new JLabel();
			label.setForeground(Color.BLACK);
			icon.paintIcon(label, g, 0, 0);
			ImageIO.write(img, "PNG", file);
		}
		executedIn.sendFile(file);
	}

	@Override
	public String getName()
	{
		return "latex";
	}

	@Override
	public String[] getAliases()
	{
		return new String[0];
	}

	@Override
	public String getHelp()
	{
		return "Affiche une image LaTeX d'une expression mathématique";
	}
}
