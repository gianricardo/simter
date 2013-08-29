/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

/**
 * The JSimProcessDeath class is an internal J-Sim exception used when a process is killed by the simulation. The JSimProcessDeath exception
 * is propagated from hold() or passivate() via life() to run() where it is caught. You should never throw or catch this exception.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.0
 */
public class JSimProcessDeath extends RuntimeException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 3156416748812388997L;

	/**
	 * Creates a new JSimProcessDeath exception.
	 */
	public JSimProcessDeath()
	{
		super("J-Sim internal exception. Do not throw it, do not catch it, do not care about it.");
	} // constructor

} // class JSimProcessDeath
