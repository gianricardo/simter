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
 * A stream generating random real values with an exponential distribution with a given lambda. The lambda parameter is set by the user when
 * the stream is constructed. An initial seed can be specified, which allows you to generate the same sequence of random values several
 * times. A complex description of exponential distribution can be found at <a
 * href="http://mathworld.wolfram.com/ExponentialDistribution.html" target="_blank"> Wolfram Research Math World pages</a>.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimExponentialStream
{
	/**
	 * An internal stream from which random numbers are taken and modified.
	 */
	protected Random stream;

	/**
	 * The lambda parameter of the distribution, also called the rate of change.
	 */
	private double lambda;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new stream generating random real values with exponential distribution. A pseudo-randomly generated seed will be used.
	 * 
	 * @param lambda
	 *            The lambda parameter of the distribution.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if lambda is less than 0.
	 */
	public JSimExponentialStream(double lambda) throws JSimInvalidParametersException
	{
		if (lambda < 0.0)
			throw new JSimInvalidParametersException("JSimExponentialStream.JSimExponentialStream(): lambda must be non-negative.");

		this.lambda = lambda;
		stream = new Random();
	} // constructor

	/**
	 * Creates a new stream generating random real values with exponential distribution. The seed given as the second argument will be used.
	 * 
	 * @param lambda
	 *            The lambda parameter of the distribution.
	 * @param seed
	 *            A seed that will initialize the generator.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if lambda is less than 0.
	 */
	public JSimExponentialStream(double lambda, long seed) throws JSimInvalidParametersException
	{
		if (lambda < 0.0)
			throw new JSimInvalidParametersException("JSimExponentialStream.JSimExponentialStream(): lambda must be non-negative.");

		this.lambda = lambda;
		stream = new Random(seed);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a randomly generated real value having exponential distribution with the given lambda parameter.
	 * 
	 * @return A random real value with exponential distribution.
	 */
	public double getNext()
	{
		double y, x;

		y = stream.nextDouble(); // y is from <0,1)
		x = (-1.0 * Math.log(1.0 - y)) / lambda;
		return x;
	} // getNext

	/**
	 * Returns the lambda parameter of the distribution.
	 * 
	 * @return The lambda parameter of the distribution.
	 */
	public double getLambda()
	{
		return lambda;
	} // getLambda

} // class JSimExponentialStream
