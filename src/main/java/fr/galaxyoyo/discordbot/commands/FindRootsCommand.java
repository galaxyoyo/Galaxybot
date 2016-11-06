package fr.galaxyoyo.discordbot.commands;

import fr.galaxyoyo.discordbot.utils.ComplexNumber;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.User;

public class FindRootsCommand implements Command
{
	public static ComplexNumber[] findRoots3Degrees(ComplexNumber a, ComplexNumber b, ComplexNumber c)
	{
		ComplexNumber sum = ComplexNumber.TWO.multiply(a.cube()).substract(new ComplexNumber(9).multiply(a).multiply(b)).add(new ComplexNumber(27).multiply(c));
		ComplexNumber prod = a.square().substract(new ComplexNumber(3).multiply(b)).cube();

		ComplexNumber[] uvRoots = findRoots2Degrees(ComplexNumber.ONE, sum.negate(), prod);
		ComplexNumber u = uvRoots[0].cubeRoot();
		ComplexNumber v = uvRoots[1].cubeRoot();

		System.out.println(u + ", " + v);

		ComplexNumber alpha = a.add(u).add(v).divide(new ComplexNumber(3));
		ComplexNumber beta, gamma;

		if (!alpha.equals(ComplexNumber.ZERO))
		{
			ComplexNumber A = a.substract(alpha);
			ComplexNumber B = c.divide(alpha);

			ComplexNumber[] roots = findRoots2Degrees(ComplexNumber.ONE, A.negate(), B);
			beta = roots[0];
			gamma = roots[1];
		}
		else if (b.equals(ComplexNumber.ZERO))
		{
			beta = ComplexNumber.ZERO;
			gamma = a;
		}
		else
		{
			ComplexNumber[] roots = findRoots2Degrees(ComplexNumber.ONE, a.negate(), b);
			beta = roots[0];
			gamma = roots[1];
		}

		return new ComplexNumber[]{alpha, beta, gamma};
	}

	public static ComplexNumber[] findRoots2Degrees(ComplexNumber a, ComplexNumber b, ComplexNumber c)
	{
		ComplexNumber delta = (b.square().substract(a.multiply(c).multiply(new ComplexNumber(4)))).squareRoot();
		System.out.println(a + ", " + b + ", " + delta);
		return new ComplexNumber[]{b.negate().add(delta).divide(a.multiply(new ComplexNumber(2))), b.negate().substract(delta).divide(a.multiply(new ComplexNumber(2)))};
	}

	@Override
	public void execute(User executor, Channel executedIn, String... args) throws Exception
	{

	}

	@Override
	public String getName()
	{
		return "findroots";
	}

	@Override
	public String[] getAliases()
	{
		return new String[]{"racines", "roots", "fr"};
	}

	@Override
	public String getHelp()
	{
		return "Permets de donner les racines d'un polynôme de degré 2 ou 3.";
	}
}
