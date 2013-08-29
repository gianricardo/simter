/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import java.io.PrintStream;

import cz.zcu.fav.kiv.jsim.JSimException;

/**
 * The JSimTooManySemaphoresException is thrown whenever J-Sim cannot add a new semaphore (JSimSemaphore) into a simulation. You can create
 * at most 9.223.372.036.854.775.807 semaphores within a simulation. You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimTooManySemaphoresException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -6791128141792934698L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimTooManySemaphoresException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimTooManySemaphoresException(String pars)
	{
		super("J-Sim cannot add a new semaphore to the simulation.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("J-Sim is not able to add a new semaphore (JSimSemaphore) to the simulation");
		ps.println("because there is no available number for that.");
		ps.println("There can be at most " + Long.MAX_VALUE + " semaphores created in a simulation.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimTooManySemaphoresException
