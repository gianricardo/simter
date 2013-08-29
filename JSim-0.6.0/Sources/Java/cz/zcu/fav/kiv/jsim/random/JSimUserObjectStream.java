/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.random;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import java.util.Random;

/**
 * A stream generating user-defined objects according to their corresponding user-defined absolute rates. The probability that a certain
 * object will be returned by the getNext() method is equal to the ratio its_absolute_rate&nbsp;/&nbsp;sum_of_all_absolute_rates. All
 * objects specified by the user must be non-null, all absolute rates must be non-negative. An initial seed can be specified, which allows
 * you to generate the same sequence of user objects several times.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimUserObjectStream
{
	/**
	 * An internal stream from which random numbers are taken to compute an object.
	 */
	protected Random stream;

	/**
	 * The objects specified by the user and returned by getNext().
	 */
	private Object[] objects;

	/**
	 * The absolute rates of object occurence. The bigger the i<sup>th</sup> rate is, the bigger probability that the i<sup>th</sup>
	 * object will be returned by getNext().
	 */
	private double[] rates;

	/**
	 * Cumulative rates of object occurence.
	 */
	private double[] cumulativeRates;

	/**
	 * The sum of all rates.
	 */
	private double sumOfRates;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new user object stream. A pseudo-randomly generated seed will be used. Both arrays are copied so any subsequent changes do
	 * not affect the generator functionality. However, in the case of user objects, just a shallow copy is performed, so any subsequent
	 * change of an object to be returned <em>does</em> possibly affect the return value of getNext().
	 * 
	 * @param objects
	 *            User objects to be returned by getNext(). No object of the array can be null. The length of the array must be positive and
	 *            equal to the lenght of rates.
	 * @param rates
	 *            The absolute rates of occurence of user-defined objects. Every rate must be non-negative. The length of the array must be
	 *            positive and equal to the lenght of objects.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the arrays of objects and rates are null, have zero length, have different lengths, or
	 *                an item of the arrays is null or less than zero, respectively.
	 */
	public JSimUserObjectStream(Object[] objects, double[] rates) throws JSimInvalidParametersException
	{
		int i;

		if ((objects == null) || (objects.length < 1))
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects");
		if ((rates == null) || (rates.length < 1))
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): rates");
		if (objects.length != rates.length)
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects.length and rates.length are different");

		// Copy the objects.
		this.objects = new Object[objects.length];
		for (i = 0; i < objects.length; i++)
			if (objects[i] != null)
				this.objects[i] = objects[i];
			else
				throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects[i] null");

		// Copy the rates.
		sumOfRates = 0.0;
		this.rates = new double[rates.length];
		for (i = 0; i < rates.length; i++)
			if (rates[i] >= 0.0)
			{
				this.rates[i] = rates[i];
				sumOfRates += rates[i];
			} // if
			else
				throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): rates[i] < 0");

		// Initialize the cumulative rates.
		cumulativeRates = new double[rates.length + 1];
		cumulativeRates[0] = 0.0;
		for (i = 1; i < cumulativeRates.length; i++)
			cumulativeRates[i] = cumulativeRates[i - 1] + rates[i - 1]; // Rates have one less element than cumulative rates!

		// Initialize the random stream.
		stream = new Random();
	} // constructor

	/**
	 * Creates a new user object stream. The seed given as the third argument will be used. Both arrays are copied so any subsequent changes
	 * do not affect the generator functionality. However, in the case of user objects, just a shallow copy is performed, so any subsequent
	 * change of an object to be returned <em>does</em> possibly affect the return value of getNext().
	 * 
	 * @param objects
	 *            User objects to be returned by getNext(). No object of the array can be null. The length of the array must be positive and
	 *            equal to the lenght of rates.
	 * @param rates
	 *            The absolute rates of occurence of user-defined objects. Every rate must be non-negative. The length of the array must be
	 *            positive and equal to the lenght of objects.
	 * @param seed
	 *            A seed that will initialize the generator.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the arrays of objects and rates are null, have zero length, have different lengths, or
	 *                an item of the arrays is null or less than zero, respectively.
	 */
	public JSimUserObjectStream(Object[] objects, double[] rates, long seed) throws JSimInvalidParametersException
	{
		int i;

		if ((objects == null) || (objects.length < 1))
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects");
		if ((rates == null) || (rates.length < 1))
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): rates");
		if (objects.length != rates.length)
			throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects.length and rates.length are different");

		// Copy the objects.
		this.objects = new Object[objects.length];
		for (i = 0; i < objects.length; i++)
			if (objects[i] != null)
				this.objects[i] = objects[i];
			else
				throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): objects[i] null");

		// Copy the rates.
		sumOfRates = 0.0;
		this.rates = new double[rates.length];
		for (i = 0; i < rates.length; i++)
			if (rates[i] >= 0.0)
			{
				this.rates[i] = rates[i];
				sumOfRates += rates[i];
			} // if
			else
				throw new JSimInvalidParametersException("JSimUserObjectStream.JSimUserObjectStream(): rates[i] < 0");

		// Initialize the cumulative rates.
		cumulativeRates = new double[rates.length + 1];
		cumulativeRates[0] = 0.0;
		for (i = 1; i < cumulativeRates.length; i++)
			cumulativeRates[i] = cumulativeRates[i - 1] + rates[i - 1]; // Rates have one less element than cumulative rates!

		// Initialize the random stream.
		stream = new Random(seed);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a randomly selected user object. The probability that a certain object is returned is equal to the ratio between its rate and
	 * the sum of all rates.
	 * 
	 * @return A randomly selected user object.
	 */
	public Object getNext()
	{
		double randomNumber;
		int index = -1;
		boolean found = false;

		randomNumber = stream.nextDouble() * sumOfRates;
		for (int i = 0; (!found) && (i < cumulativeRates.length - 1); i++)
		{
			// Compare the generated number with the interval.
			// Cumulative rates have 0.0 at position 0 and their length is rates.length+1.
			if ((i == cumulativeRates.length - 2) || ((randomNumber >= cumulativeRates[i]) && (randomNumber < cumulativeRates[i + 1])))
			{
				index = i;
				found = true;
			} // if
		} // for i

		if (index != -1)
			return objects[index];
		else
			return null;
	} // getNext

	/**
	 * Returns a copy of user objects returned by getNext(). Subsequent changes to the return value (an array of Object instances) does not
	 * affect the generator but any change to an item of the returned array <em>does</em> affect it.
	 * 
	 * @return A copy of user objects returned by getNext().
	 */
	public Object[] getObjects()
	{
		Object[] array;

		array = new Object[objects.length];
		for (int i = 0; i < objects.length; i++)
			array[i] = objects[i];

		return array;
	} // getObjects

	/**
	 * Returns a copy of rates used by the generator. Subsequent changes to the return value (an array of doubles) does not affect the
	 * generator.
	 * 
	 * @return A copy of rates used by the generator.
	 */
	public double[] getRates()
	{
		double[] array;

		array = new double[rates.length];
		for (int i = 0; i < rates.length; i++)
			array[i] = rates[i];

		return array;
	} // getRates

} // class JSimUserObjectStream
