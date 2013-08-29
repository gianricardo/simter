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
 * The JSimSecurityException is thrown whenever the user tries to execute an action that is forbidden in the current simulation context. You
 * should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.2
 */
public class JSimSecurityException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 7648761331634727514L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimSecurityException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a varibale's name.
	 */
	public JSimSecurityException(String pars)
	{
		super("J-Sim cannot execute the required action because of a (potential) security violation.", pars);
	} // constructor

	/**
	 * Creates a new JSimSecurityException with a specific information and a specified cause.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a varibale's name.
	 * @param cause
	 *            The original exception that caused this exception to be thrown.
	 */
	public JSimSecurityException(String pars, Throwable cause)
	{
		super("J-Sim cannot execute the required action because of a (potential) security violation.", pars, cause);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("It is not allowed to execute the action you required because");
		ps.println("the action could demage J-Sim internal structures");
		ps.println("or cause J-Sim to hang or operate improperly.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimSecurityException
