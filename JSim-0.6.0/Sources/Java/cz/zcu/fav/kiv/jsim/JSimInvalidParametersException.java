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
 * The JSimInvalidParametersException is thrown whenever a method or a constructor of a J-Sim class receives arguments that cannot be used.
 * You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.1
 */
public class JSimInvalidParametersException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -3084914898257005612L;
	
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimInvalidParametersException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example method's or a variable's name.
	 */
	public JSimInvalidParametersException(String pars)
	{
		super("You passed one or more parameters with invalid value to a method.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("Your parameters are invalid and have been rejected.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimInvalidParametersException
