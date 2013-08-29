/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.gui;

import java.util.Collection;

import javax.swing.JDialog;

/**
 * An object that implements the JSimDisplayable interface can be displayed in a component of the main GUI window.
 * 
 * @author Pavel DOMECKÝ
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.2.0
 */
public interface JSimDisplayable
{
	/**
	 * Returns a string containing basic informations about an object. The string will be displayed in a JSimMainWindowList component.
	 * 
	 * @return A string containing basic informations about an object.
	 */
	public String getObjectListItemDescription();

	/**
	 * Returns a collection of object's characteristics. Always return a collection of JSimPair objects. The collection will be displayed in
	 * a JSimDetailedInfoWindow table.
	 * 
	 * @return A collection of object's characteristics.
	 */
	public Collection<JSimPair> getDetailedInformationArray();

	/**
	 * Creates a detailed info window that shows information about an object. Returns a reference to the created window.
	 * 
	 * @return A reference to the created info window.
	 */
	public JDialog createDetailedInfoWindow(JSimMainWindow parentWindow);

} // interface JSimDisplayable
