package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.DiscordBot;
import org.python.core.*;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.util.MessageBuilder;

import java.io.StringBufferInputStream;
import java.io.StringWriter;

public class PythonEvalCommand implements Command
{
	private static final PyStringMap vars = new PyStringMap();

	@Override
	public void execute(User executor, Channel channel, String... args) throws Exception
	{
		if (!channel.getName().contains("dev"))
		{
			channel.sendMessage("Cette commande est en cours de développement.");
			return;
		}

		new MessageBuilder(DiscordBot.getClient()).withChannel(channel).withContent("Exécution de Python :").appendCode("python", String.join(" ", args)).build();

		String code = "from java.lang import *\n"
				+ "from java.util import *\n"
				+ "from java.awt import *\n"
				+ "from javax.swing import * \n"
				+ "from fr.galaxyoyo.discordbot import *\n\n";
		code += String.join("", args);

		try
		{
			//noinspection deprecation
			PyCode pyCode = Py.compile(new StringBufferInputStream(code), "__run__.py", CompileMode.exec);
			StringWriter sw = new StringWriter();
			vars.__setitem__("sys.stdout", Py.java2py(sw));
			PyObject obj = Py.runCode(pyCode, null, vars);
			System.out.println(obj + ", " + obj.getClass());
			System.out.println(sw);
		}
		catch (PyException ex)
		{
			channel.sendMessage("Erreur lors de l’exécution : " + ex.traceback.asString());
		}
		catch (Exception ex)
		{
			channel.sendMessage("Erreur lors de l’exécution : " + ex.getMessage());
		}
	}

	@Override
	public String getName()
	{
		return "python";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"py"};
	}

	@Override
	public String getHelp()
	{
		return "Lance un script python";
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}
}
