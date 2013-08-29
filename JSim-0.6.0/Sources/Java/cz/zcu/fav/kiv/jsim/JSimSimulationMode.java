/*
 * Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 * Licensed under the Academic Free License version 2.1
 * J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 * 
 */

package cz.zcu.fav.kiv.jsim;

/**
 * The simulation mode defines some properties and capabilities of a simulation. Currently there are three modes available:
 * <ul>
 * <li>Text</li>
 * <li>Batch GUI</li>
 * <li>Interactive GUI</li>
 * </ul>
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.6.0
 */
public enum JSimSimulationMode
{
	/**
	 * Output goes to console only. The step() method is called explicitely from simulation code, no simulation control is available. 
	 */
	TEXT("Text"),
	
	/**
	 * Output goes to a GUI window. The step() method is called explicitely from simulation code, no simulation control is available.
	 */
	GUI_BATCH("Batch GUI"),
	
	/**
	 * Output goes to a GUI window. The step() method is called upon user request.
	 */
	GUI_INTERACTIVE("Interactive GUI");

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * A human-readable description of the simulation mode.
	 */
	private final String humanReadableDescription;

	/**
	 * Creates a simulation mode.
	 * 
	 * @param humanReadableDescription
	 *            A human-readable description.
	 */
	private JSimSimulationMode(String humanReadableDescription)
	{
		this.humanReadableDescription = humanReadableDescription;
	} // constructor

	/**
	 * Returns a human-readable description of the simulation mode.
	 * 
	 * @return A human-readable description of the simulation mode.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	public String toString()
	{
		return humanReadableDescription;
	} // toString

} // enum JSimSimulationMode
