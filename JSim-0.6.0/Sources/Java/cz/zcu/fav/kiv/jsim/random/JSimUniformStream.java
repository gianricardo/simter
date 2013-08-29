/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.random;

import java.util.Random;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;

/**
 * A stream generating random real values with a uniform distribution on a given interval. The bounds of the interval are set by the user
 * when the stream is constructed. An initial seed can be specified, which allows you to generate the same sequence of random values several
 * times. A complex description of uniform distribution can be found at <a href="http://mathworld.wolfram.com/UniformDistribution.html"
 * target="_blank"> Wolfram Research Math World pages</a>.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimUniformStream
{
	/**
	 * An internal stream from which random numbers are taken and modified.
	 */
	protected Random stream;

	/**
	 * The lower bound of the interval from which random numbers are generated.
	 */
	private double a;

	/**
	 * The upper bound of the interval from which random numbers are generated.
	 */
	private double b;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new stream generating random real values from a given interval. A pseudo-randomly generated seed will be used.
	 * 
	 * @param a
	 *            The lower bound of the interval.
	 * @param b
	 *            The upper bound of the interval.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if a is not less than b.
	 */
	public JSimUniformStream(double a, double b) throws JSimInvalidParametersException
	{
		if (a >= b)
			throw new JSimInvalidParametersException("JSimUniformStream.JSimUniformStream(): `a' must be less than `b'.");

		this.a = a;
		this.b = b;
		stream = new Random();
	} // constructor

	/**
	 * Creates a new stream generating random real values from a given interval. The seed given as the third argument will be used.
	 * 
	 * @param a
	 *            The lower bound of the interval.
	 * @param b
	 *            The upper bound of the interval.
	 * @param seed
	 *            A seed that will initialize the generator.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if a is not less than b.
	 */
	public JSimUniformStream(double a, double b, long seed) throws JSimInvalidParametersException
	{
		if (a >= b)
			throw new JSimInvalidParametersException("JSimUniformStream.JSimUniformStream(): `a' must be less than `b'.");

		this.a = a;
		this.b = b;
		stream = new Random(seed);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a randomly generated real value from the interval specified on this stream's construction.
	 * 
	 * @return A random real value from the given interval.
	 */
	public double getNext()
	{
		double randomNumber;

		// To get <a,b> instead of <a,b), we substract the random number from 1 in half cases.
		randomNumber = stream.nextDouble();
		if (stream.nextDouble() < 0.5)
                {
			randomNumber = 1.0 - randomNumber;
                }

		return ((randomNumber * (b - a)) + a);
	} // getNext

	/**
	 * Returns the lower bound of the interval.
	 * 
	 * @return The lower bound of the interval.
	 */
	public double getA()
	{
		return a;
	} // getA

	/**
	 * Returns the upper bound of the interval.
	 * 
	 * @return The upper bound of the interval.
	 */
	public double getB()
	{
		return b;
	} // getB

} // class JSimUniformStream
