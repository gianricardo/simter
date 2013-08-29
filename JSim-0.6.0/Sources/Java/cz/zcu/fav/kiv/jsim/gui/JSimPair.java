/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.gui;

/**
 * The JSimPair holds information about two objects &ndash; a name and a value. Use this class if you want to add a new new pair to the detailed
 * info window of your class implementing JSimDisplayable.
 * 
 * @author Pavel DOMECKÝ
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.2.0
 */
public class JSimPair
{
	/**
	 * The characteristic's name.
	 */
	private String name;

	/**
	 * The characteristic's current value.
	 */
	private Object value;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimPair object, holding information about the name and the value of an object's characteristics.
	 * 
	 * @param name
	 *            The characteristic's name.
	 * @param value
	 *            The characteristic's value.
	 */
	public JSimPair(String name, Object value)
	{
		this.name = name;
		this.value = value;
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the characteristic's name.
	 * 
	 * @return The characteristic's name.
	 */
	public String getName()
	{
		return name;
	} // getName

	/**
	 * Returns the characteristic's value.
	 * 
	 * @return The characteristic's value.
	 */
	public Object getValue()
	{
		return value;
	} // getValue

} // class JSimPair
