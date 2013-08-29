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
 * A stream generating random boolean values. The probability that true is generated is determined by the user when the stream is
 * constructed. An initial seed can be specified, which allows you to generate the same sequence of boolean random values several times.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimDrawStream
{
	/**
	 * An internal stream from which random numbers are taken and modified.
	 */
	protected Random stream;

	/**
	 * The probability that true will be generated each time getNext() is invoked.
	 */
	private double probability;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new stream generating random boolean values. A pseudo-randomly generated seed will be used.
	 * 
	 * @param probability
	 *            The probability that true will be returned by getNext().
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the given probability is out of the interval &lt;0,1&gt;.
	 */
	public JSimDrawStream(double probability) throws JSimInvalidParametersException
	{
		if ((probability < 0.0) || (probability > 1.0))
			throw new JSimInvalidParametersException("JSimDrawStream.JSimDrawStream(): probability out of <0,1>.");

		this.probability = probability;
		stream = new Random();
	} // constructor

	/**
	 * Creates a new stream generating random boolean values. The seed given as the second argument will be used.
	 * 
	 * @param probability
	 *            The probability that true will be returned by getNext().
	 * @param seed
	 *            A seed that will initialize the generator.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the given probability is out of the interval &lt;0,1&gt;.
	 */
	public JSimDrawStream(double probability, long seed) throws JSimInvalidParametersException
	{
		if ((probability < 0.0) || (probability > 1.0))
			throw new JSimInvalidParametersException("JSimDrawStream.JSimDrawStream(): probability out of <0,1>.");

		this.probability = probability;
		stream = new Random(seed);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a randomly generated boolean value. The probability that true is returned is equal to the probability specified by the user
	 * when the stream is created.
	 * 
	 * @return A random boolean value.
	 */
	public boolean getNext()
	{
		return (stream.nextDouble() < probability);
	} // getNext

	/**
	 * Returns the probability that true is generated by getNext().
	 * 
	 * @return The probability that true is generated by getNext().
	 */
	public double getProbability()
	{
		return probability;
	} // getProbability

} // class JSimDrawStream