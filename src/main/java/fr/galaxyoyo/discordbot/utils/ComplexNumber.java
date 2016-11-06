package fr.galaxyoyo.discordbot.utils;

public class ComplexNumber extends Number
{
	public static final ComplexNumber ZERO = new ComplexNumber(0, 0);
	public static final ComplexNumber ONE = new ComplexNumber(1, 0);
	public static final ComplexNumber TWO = new ComplexNumber(2, 0);
	public static final ComplexNumber I = new ComplexNumber(0, 1);

	private final double realPart;
	private final double imaginaryPart;

	public ComplexNumber(int real)
	{
		this(real, 0);
	}

	public ComplexNumber(double real, double imaginary)
	{
		this.realPart = real;
		this.imaginaryPart = imaginary;
	}

	@Override
	public int intValue()
	{
		return (int) realPart;
	}

	@Override
	public long longValue()
	{
		return (long) realPart;
	}

	@Override
	public float floatValue()
	{
		return (float) realPart;
	}

	@Override
	public double doubleValue()
	{
		return realPart;
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits(realPart);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(imaginaryPart);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ComplexNumber that = (ComplexNumber) o;

		return Double.compare(that.realPart, realPart) == 0 && Double.compare(that.imaginaryPart, imaginaryPart) == 0;
	}

	@Override
	public String toString()
	{
		return realPart + (imaginaryPart != 0 ? (imaginaryPart < 0 ? " - " : " +") + Math.abs(imaginaryPart) + "i" : "");
	}

	public ComplexNumber cube()
	{
		return multiply(square());
	}

	public ComplexNumber multiply(ComplexNumber number)
	{
		return new ComplexNumber(realPart() * number.realPart() - imaginaryPart() * number.imaginaryPart(),
				realPart() * number.imaginaryPart() - imaginaryPart() * number.realPart());
	}

	public ComplexNumber square()
	{
		return multiply(this);
	}

	public double realPart()
	{
		return realPart;
	}

	public double imaginaryPart()
	{
		return imaginaryPart;
	}

	public ComplexNumber substract(ComplexNumber number)
	{
		return add(number.negate());
	}

	public ComplexNumber add(ComplexNumber number)
	{
		return new ComplexNumber(realPart() + number.realPart(), imaginaryPart() + imaginaryPart());
	}

	public ComplexNumber negate()
	{
		return multiply(new ComplexNumber(-1, 0));
	}

	public ComplexNumber squareRoot()
	{
		if (imaginaryPart == 0)
		{
			return new ComplexNumber(Math.sqrt(Math.abs(realPart)), realPart < 0 ? 1 : 0);
		}

		return this;
	}

	public ComplexNumber cubeRoot()
	{
		return new ComplexNumber(Math.cbrt(realPart), Math.cbrt(imaginaryPart));
	}

	public ComplexNumber divide(ComplexNumber number)
	{
		return new ComplexNumber(realPart() / number.realPart() - imaginaryPart() / number.imaginaryPart(),
				realPart() / number.imaginaryPart() - imaginaryPart() / number.realPart());
	}
}
