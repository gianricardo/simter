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
 * The JSimInvalidProcessStateException is thrown whenever an attempt is made to change a process's state with JSimProcess.setProcessState()
 * but there is not a transition between the current and the desired state in the graph of J-Sim process states. You should never need to
 * create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.2
 */
public class JSimInvalidProcessStateException extends JSimException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -405684069016999152L;

	private JSimProcessState oldState;
	private JSimProcessState newState;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimInvalidProcessStateException with a specific information.
	 * 
	 * @param param
	 *            Holds detailed information about specific circumstances of this exception, for example a method's or a varibale's name.
	 * @param oldSt
	 *            The current state of a process.
	 * @param newSt
	 *            The state that the user or J-Sim attempted to switch to.
	 */
	public JSimInvalidProcessStateException(String param, JSimProcessState oldSt, JSimProcessState newSt)
	{
		super("You attempted to switch the process to a state which is not allowed now.", param);
		oldState = oldSt;
		newState = newSt;
	} // constructor

	/* (non-Javadoc)
	 * @see cz.zcu.fav.kiv.jsim.JSimException#printComment(java.io.PrintStream)
	 */
	public void printComment(PrintStream ps)
	{
		ps.println("The current state of the process does not allow you to switch it to the state you are requesting.");
		ps.println("Current state: "   + oldState);
		ps.println("Requested state: " + newState);
		ps.println();

		if (getSpecificInfo() != null)
		{
			ps.println("Additional information: " + getSpecificInfo());
			ps.println();
		} // if
	} // printComment

} // class JSimInvalidProcessStateException
