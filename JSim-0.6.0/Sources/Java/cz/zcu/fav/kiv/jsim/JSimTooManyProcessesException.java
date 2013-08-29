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
 * The JSimTooManyProcessesException is thrown whenever J-Sim cannot add a new process into a simulation. You can create at most
 * 9.223.372.036.854.775.807 processes within a simulation. You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.0
 */
public class JSimTooManyProcessesException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -2927090452133529374L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimTooManyProcessesException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimTooManyProcessesException(String pars)
	{
		super("J-Sim cannot add new processes to the simulation.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("J-Sim is not able to add a new process to the simulation");
		ps.println("because there is no available number for that.");
		ps.println("There can be at most " + Long.MAX_VALUE + " processes created in a simulation.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimTooManyProcessesException
