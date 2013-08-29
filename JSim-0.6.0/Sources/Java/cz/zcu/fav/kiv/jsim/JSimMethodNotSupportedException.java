/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

import java.io.PrintStream;

/**
 * The JSimMethodNotSupportedException is thrown whenever the user tries to call a method that is not supported in the current simulation
 * mode. You should never need to create an instance of this class.
 * 
 * @author Pavel DOMECKÝ
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.2.0
 */
public class JSimMethodNotSupportedException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -3900064906080300042L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimMethodNotSupportedException with specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a varibale's name.
	 */
	public JSimMethodNotSupportedException(String pars)
	{
		super("You called a method that is not supported in the current simulation mode.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("It is not allowed to call this method in this simulation mode.");
		ps.println("Calling this method in the current mode would have no meaning.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		}
	} // printComment

} // class JSimMethodNotSupportedException
