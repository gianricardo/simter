/*
 * Copyright (c) 2006 Jaroslav Kačer <jaroslav@kacer.biz>
 * Licensed under the Academic Free License version 2.1
 * J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

/**
 * During simulation program execution, the simulation object can be in one of the following states:
 * <ul>
 * <li>Not Started Yet</li>
 * <li>In Progress</li>
 * <li>Terminated</li>
 * </ul>
 *
 * @author Jarda KAČER
 *
 *
 * @version J-Sim version 0.6.0
 *
 * @since J-Sim version 0.6.0
 */
public enum JSimSimulationState
{
	/**
	 * 
	 */
	NOT_STARTED("Not Started Yet"),
	
	/**
	 * 
	 */
	IN_PROGRESS("In Progress"),
	
	/**
	 * 
	 */
	TERMINATED("Terminated");
	
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * A human-readable description of the simulation state.
	 */
	private final String humanReadableDescription;

	/**
	 * Creates a simulation mode.
	 * 
	 * @param humanReadableDescription
	 *            A human-readable description.
	 */
	private JSimSimulationState(String humanReadableDescription)
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

} // enum JSimSimulationState
