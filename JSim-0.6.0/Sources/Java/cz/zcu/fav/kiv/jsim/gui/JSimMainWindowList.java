/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;

/**
 * A JSimMainWindowList is a list whose items are simple descriptions of processes, queues, or other simulation elements. The elements must
 * implement JSimDisplayable.
 * 
 * @author Pavel DOMECKÝ
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.2
 */
public class JSimMainWindowList extends JList implements Observer
{
	/**
	 * Serialization identification. 
	 */
	private static final long serialVersionUID = -1008381964311514679L;

	/**
	 * Common logger for all instances of this class. By default, all logging information goes to a file. Only severe events go to the
	 * console, in addition to a file.
	 */
	private static final Logger logger;

	/**
	 * The simulation that this list's main window belongs to.
	 */
	private JSimSimulation myParent;

	/**
	 * A set of objects delivered by the simulation that this list has to display. If the set is ordered, the list will also be ordered.
	 */
	private Set<? extends JSimDisplayable> objectsToDisplay;

	/**
	 * An array list of displayed objects. A selected item's position is used as an index to this array list.
	 */
	private ArrayList<JSimDisplayable> myInfoTable;

	/**
	 * The list's list model.
	 */
	private DefaultListModel listModel;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The static block initializes all static attributes.
	 */
	static
	{
		logger = Logger.getLogger(JSimMainWindowList.class.getName());
	} // static

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JList whose items will be descriptions of processes or queues.
	 * 
	 * @param parent
	 *            The simulation object that owns the main window that this list belongs to.
	 * @param listType
	 *            The type of the list: Will it display processes or queues?
	 */
	public JSimMainWindowList(JSimSimulation parent, int listType)
	{
		super();
		try
		{
			myParent = parent;
			myInfoTable = new ArrayList<JSimDisplayable>();
			objectsToDisplay = myParent.getObjectsToBeDisplayed(listType);
			listModel = new DefaultListModel();
			this.setModel(listModel);
			setFont(new Font("SansSerif", Font.PLAIN, 10));
		} // try
		catch (JSimInvalidParametersException e)
		{
			objectsToDisplay = null;
			setModel(null);
			logger.log(Level.WARNING, "An invalid list type was specified.", e);
		} // catch
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Updates the items in the list if the state of displayed objects has changed since last call of update().
	 */
	public void update(Observable o, Object arg)
	{
		Iterator<? extends JSimDisplayable> it;
		JSimDisplayable jsd;
		String description;

		listModel.clear();
		myInfoTable.clear();

		it = objectsToDisplay.iterator();
		while (it.hasNext())
		{
			jsd = it.next();
			description = jsd.getObjectListItemDescription();
			if (description == null)
				description = "<Description not available>";
			listModel.addElement(description);
			myInfoTable.add(jsd);
		} // while has next
	} // update

	/**
	 * Return a table of all items displayed in the list. It is important to provide such an ordered list because the main window will
	 * access the displayed objects via their indices when a double-click occurs.
	 * 
	 * @return An array list containing all displayed objects in the same order that they are displayed.
	 */
	protected ArrayList<JSimDisplayable> getInfoTable()
	{
		return myInfoTable;
	} // getInfoTable

} // class JSimMainWindowList

