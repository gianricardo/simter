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
 * The JSimSimulationAlreadyTerminatedException is thrown whenever the user attempts to add a new process into a terminated simulation.
 * Simulation is terminated if there are no more scheduled events or there are no more processes. You should never need to create an
 * instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.0
 */
public class JSimSimulationAlreadyTerminatedException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -63684748449634111L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimSimulationAlreadyRunningException with a specific information.
	 * 
	 * @param pars
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a variable's name.
	 */
	public JSimSimulationAlreadyTerminatedException(String pars)
	{
		super("You cannot add new processes to a terminated simulation.", pars);
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("If a simulation has already terminated");
		ps.println("it is not allowed to add new processes into it.");
		ps.println("You should reinitialize the simulation or avoid inserting");
		ps.println("new processes at this time.");
		ps.println("Please check up your source code.");
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimSimulationAlreadyTerminatedException
