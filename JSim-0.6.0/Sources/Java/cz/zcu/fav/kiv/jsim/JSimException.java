/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

import java.io.PrintStream;

/**
 * The JSimException class is a general exception used by J-Sim to inform user about an error. Unlike its subclasses, it is almost never
 * instantiated. You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.1
 */
public class JSimException extends Exception
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 616365517273053349L;
	
	/**
	 * Specific message for every exception.
	 */
	private String specific;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimException with a description and a specific information. Descendants will use this constructor to pass their own
	 * data to the Exception constructor.
	 * 
	 * @param description
	 *            Describes the error that occured.
	 * @param param
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimException(String description, String param)
	{
		super(description);
		specific = param;
	} // constructor

	/**
	 * Creates a new JSimException with the default description and a specific information.
	 * 
	 * @param param
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimException(String param)
	{
		this("This is a general J-Sim exception.", param);
	} // constructor

	/**
	 * Creates a new JSimException with a description, a specific information, and the cause of this exception. Descendants will use this
	 * constructor to pass their own data to the Exception constructor.
	 * 
	 * @param description
	 *            Describes the error that occured.
	 * @param param
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 * @param cause
	 *            The original exception that caused this exception to be thrown.
	 */
	public JSimException(String description, String param, Throwable cause)
	{
		super(description, cause);
		specific = param;
	} // constructor

	/**
	 * Creates a new JSimException with the default description, a specific information, and the cause of this exception.
	 * 
	 * @param param
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 * @param cause
	 *            The original exception that caused this exception to be thrown.
	 */
	public JSimException(String param, Throwable cause)
	{
		this("This is a general J-Sim exception.", param, cause);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Prints an explanation why this exception was thrown. The explanation is printed out to a print stream.
	 * 
	 * @param ps
	 *            The print stream that the explanation is to be printed to.
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("This exception is thrown whenever something suspicious or dangerous happens");
		ps.println("inside your simulation but it is too difficult to determine");
		ps.println("the exact reason of this abnormality.");
		ps.println("Please check up your source code.");
		ps.println();

		if (specific != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

	/**
	 * Prints an explanation why this exception was thrown. The explanation is printed out to standard error output.
	 */
	public void printComment()
	{
		printComment(System.err);
	} // printComment

	/**
	 * Returns detailed information about specific circumstances of the exception.
	 * 
	 * @return Detailed information about specific circumstances of the exception.
	 */
	public String getSpecificInfo()
	{
		return specific;
	} // getSpecificInfo

} // class JSimException
