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
 * The JSimInvalidContextException is thrown whenever the user tries to call a method that cannot be invoked from the piece of code
 * currently being executed. For example, a semaphore's P() operation can only be invoked from a process's code, not from outside a process.
 * You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimInvalidContextException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 7223601046880769736L;
	
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimInvalidContextException with specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a varibale's name.
	 */
	public JSimInvalidContextException(String pars)
	{
		super("You called a method that cannot be invoked safely in the current context.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("It is not allowed to call this method just now.");
		ps.println("You are probably calling a method whose invocation is restricted");
		ps.println("to some calees only, e.g. processes.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		}
	} // printComment

} // class JSimInvalidContextException
