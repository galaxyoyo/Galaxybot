package fr.galaxyoyo.discordbot;

import fr.galaxyoyo.discordbot.commands.JSEvalCommand;

import java.io.FilePermission;
import java.security.Permission;
import java.util.Objects;

public class GalaxySecurityManager extends SecurityManager
{
	@Override
	public void checkPermission(Permission perm)
	{
		if (!isCalledFromJS())
			return;

		if (perm instanceof FilePermission)
		{
			FilePermission fp = (FilePermission) perm;
			if (!fp.getName().contains("tmp") && (Objects.equals(fp.getActions(), "write") || Objects.equals(fp.getActions(), "delete")))
				throw new SecurityException("Vous n’êtes pas autorisé à écrire ni à supprimer.");
		}
		else if (perm instanceof RuntimePermission)
		{
			RuntimePermission rp = (RuntimePermission) perm;
			if (rp.getName().equals("setSecurityManager"))
				throw new SecurityException("Vous n’êtes pas autorisé à modifier le SecurityManager.");
			else if (rp.getName().startsWith("exitVM."))
				throw new SecurityException("Vous n’êtes pas autorisé à quitter la JVM.");
		}
	}

	public boolean isCalledFromJS()
	{
		for (Class<?> clazz : getClassContext())
		{
			if (clazz == JSEvalCommand.class)
				return true;
		}

		return false;
	}
}
