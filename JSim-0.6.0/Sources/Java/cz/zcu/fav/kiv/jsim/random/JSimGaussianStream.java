/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.random;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;

/**
 * A stream generating random real values with a Gaussian (aka normal) distribution with given mu and sigma. The two parameters are set by
 * the user when the stream is constructed. An initial seed can be specified, which allows you to generate the same sequence of random
 * values several times. A complex description of Gaussian (normal) distribution can be found at <a
 * href="http://mathworld.wolfram.com/NormalDistribution.html" target="_blank"> Wolfram Research Math World pages</a>.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimGaussianStream
{
	/**
	 * An internal stream from which random numbers are taken and modified. Because we need the stream to generate values with a uniform
	 * distribution from &lt;0,1&gt;, JSimUniformStream is used instead the classic Java Random.
	 */
	protected JSimUniformStream stream;

	/**
	 * The mu parameter of the distribution, equal to the mean value.
	 */
	private double mu;

	/**
	 * The sigma parameter of the distribution, equal to the square root of the variance.
	 */
	private double sigma;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new stream generating random real values with Gaussian distribution. A pseudo-randomly generated seed will be used.
	 * 
	 * @param mu
	 *            The mu parameter of the distribution, equal to the mean value.
	 * @param sigma
	 *            The sigma parameter of the distribution, equal to the square root of the variance.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if sigma is less than 0.
	 */
	public JSimGaussianStream(double mu, double sigma) throws JSimInvalidParametersException
	{
		if (sigma < 0.0)
			throw new JSimInvalidParametersException("JSimGaussianStream.JSimGaussianStream(): sigma must be non-negative.");

		this.mu = mu;
		this.sigma = sigma;
		stream = new JSimUniformStream(0.0, 1.0);
	} // constructor

	/**
	 * Creates a new stream generating random real values with Gaussian distribution. The seed given as the third argument will be used.
	 * 
	 * @param mu
	 *            The mu parameter of the distribution, equal to the mean value.
	 * @param sigma
	 *            The sigma parameter of the distribution, equal to the square root of the variance.
	 * @param seed
	 *            A seed that will initialize the generator.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if sigma is less than 0.
	 */
	public JSimGaussianStream(double mu, double sigma, long seed) throws JSimInvalidParametersException
	{
		if (sigma < 0.0)
			throw new JSimInvalidParametersException("JSimGaussianStream.JSimGaussianStream(): sigma must be non-negative.");

		this.mu = mu;
		this.sigma = sigma;
		stream = new JSimUniformStream(0.0, 1.0, seed);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a randomly generated real value having Gaussian distribution with the given mu and sigma parameters.
	 * 
	 * @return A random real value with Gaussian distribution.
	 */
	public double getNext()
	{
		double sum = 0.0;

		for (int i = 0; i < 12; i++)
			sum += stream.getNext();

		return (sigma * (sum - 6.0) + mu);
	} // getNext

	/**
	 * Returns the mu parameter of the distribution.
	 * 
	 * @return The mu parameter of the distribution.
	 */
	public double getMu()
	{
		return mu;
	} // getMu

	/**
	 * Returns the sigma parameter of the distribution.
	 * 
	 * @return The sigma parameter of the distribution.
	 */
	public double getSigma()
	{
		return sigma;
	} // getSigma

} // class JSimGaussianStream
