/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.gui;

import java.util.Observable;

/**
 * JSimChange instances can register listeners to which information about a change in the simulation is sent.
 * 
 * @author Pavel DOMECKÝ
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.2.0
 */
public class JSimChange extends Observable
{
	/**
	 * Sets the change flag on and notifies all registered listeners to display this change in the GUI.
	 */
	public void changed()
	{
		setChanged();
		notifyObservers();
	} // changed

} // class JSimChange
