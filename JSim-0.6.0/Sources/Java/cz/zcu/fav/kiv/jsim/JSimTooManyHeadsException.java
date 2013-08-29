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
 * The JSimTooManyHeadsException is thrown whenever J-Sim cannot add a new queue (JSimHead) into a simulation. You can create at most
 * 9.223.372.036.854.775.807 queues within a simulation. You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimTooManyHeadsException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 4291053650988373368L;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimTooManyHeadsException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimTooManyHeadsException(String pars)
	{
		super("J-Sim cannot add new queue to the simulation.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("J-Sim is not able to add a new queue (JSimHead) to the simulation");
		ps.println("because there is no available number for that.");
		ps.println("There can be at most " + Long.MAX_VALUE + " queues created in a simulation.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimTooManyHeadsException
