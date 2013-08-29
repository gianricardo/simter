/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

import java.util.Locale;

/**
 * The JSimSystem class provides various services, such as random-numbers generation, version functions, etc. All method are static sou you
 * should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.2
 */
public class JSimSystem
{
	/**
	 * Major version number of J-Sim.
	 */
	private static final int VERSION_MAJOR = 0;

	/**
	 * Minor version number of J-Sim. This number changes when new features are added to J-Sim.
	 */
	private static final int VERSION_MINOR = 6;

	/**
	 * Patch version number of J-Sim. This number changes when a bug is patched or a trivial change is made.
	 */
	private static final int VERSION_PATCH = 0;

	/**
	 * Locale information about this version of J-Sim. Only US English is supported now.
	 */
	private static final Locale localeInfo;

	// ------------------------------------------------------------------------------------------------------------------------------------

	static
	{
		localeInfo = Locale.US;
	} // static block

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the major version number.
	 * 
	 * @return The major version number.
	 */
	public static int getVersionMajor()
	{
		return VERSION_MAJOR;
	} // getVersionMajor

	/**
	 * Returns the minor version number.
	 * 
	 * @return The minor version number.
	 */
	public static int getVersionMinor()
	{
		return VERSION_MINOR;
	} // getVersionMinor

	/**
	 * Returns the patch version number.
	 * 
	 * @return The patch version number.
	 */
	public static int getVersionPatch()
	{
		return VERSION_PATCH;
	} // getVersionPatch

	/**
	 * Returns J-Sim version as string.
	 * 
	 * @return J-Sim version as string.
	 */
	public static String getVersion()
	{
		String s1, s2, s3, s;

		s1 = Integer.toString(getVersionMajor());
		s2 = Integer.toString(getVersionMinor());
		s3 = Integer.toString(getVersionPatch());

		s = "J-Sim version " + s1 + "." + s2 + "." + s3 + " " + localeInfo.toString();
		return s;
	} // getVersion

	/**
	 * Prints out information about J-Sim. The information is printed out to the standard output.
	 */
	public static void hello()
	{
		System.out.println("Hello! This is " + getVersion() + ".");
		System.out.println("For more information, look at: http://www.j-sim.zcu.cz");
	} // hello

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a random number having exponential distribution with parameter "lambda". If "lambda" is equal to zero, infinity or NaN can be
	 * returned. If it is negative, its absolute value is used instead.
	 * 
	 * @param lambda
	 *            Parameter "lambda" of the exponential-distribution generator.
	 * 
	 * @return A random number having exponential distribution with the given parameter "lambda".
	 */
	public static double negExp(double lambda)
	{
		double y, x;

		lambda = Math.abs(lambda);
		y = Math.random(); // y is from <0,1)

		x = (-1.0 * Math.log(1.0-y)) / lambda;
		return x;
	} // negExp

	/**
	 * Returns a random number having uniform distribution with parameters "a" and "b".
	 * 
	 * @param a
	 *            The lower bound of the interval &lt;a,b&gt;.
	 * @param b
	 *            The upper bound of the interval &lt;a,b&gt;.
	 * 
	 * @return A random number having uniform distribution with parameters "a" and "b".
	 */
	public static double uniform(double a, double b)
	{
		double randomNumber;

		if (b < a)
		{
			double xchg;
			xchg = b;
			b = a;
			a = xchg;
		} // if

		// To get <a,b> instead of <a,b), we substract the random number from 1 in half cases.
		randomNumber = Math.random();
		if (Math.random() < 0.5)
			randomNumber = 1.0 - randomNumber;

		return ((randomNumber * (b-a)) + a);
	} // uniform

	/**
	 * Returns true with a given probability.
	 * 
	 * @param probability
	 *            The probability that true will be returned.
	 * 
	 * @return True if a randomly generated number from &lt;0,1) is less than or equal to the parameter, false otherwise.
	 */
	public static boolean draw(double probability)
	{
		return (Math.random() < probability);
	} // draw

	/**
	 * Returns a random number having Gaussian (normal) distribution with parameters "mu" and "sigma".
	 * 
	 * @param mu
	 *            The parameter "mu" (mean value) of the normal-distribution generator.
	 * @param sigma
	 *            The parameter "sigma" (variance) of the normal-distribution generator.
	 * 
	 * @return A random number having normal distribution with the given parameters "mu" and "sigma".
	 */
	public static double gauss(double mu, double sigma)
	{
		int i;
		double sum;

		sum = 0.0;
		for (i = 0; i < 12;  i++)
			sum += uniform(0.0, 1.0);

		return (sigma * (sum - 6.0) + mu);
	} // gauss

} // class JSimSystem
