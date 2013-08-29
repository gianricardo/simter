/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import cz.zcu.fav.kiv.jsim.JSimProcess;

/**
 * All message classes implementing this interface assure that their sending process can be obtained and it is a single specific process,
 * not a set of processes, all processes, or an unspecified process.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public interface JSimMessageWithKnownSender
{
	/**
	 * Provides the sender of the message.
	 * 
	 * @return The sender of the message.
	 */
	public JSimProcess getSender();

} // interface JSimMessageWithKnownSender
