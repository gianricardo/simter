/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The JSimDetailedInfoWindow provides a dialog that shows a table with detailed information about a process, a queue, or any
 * JSimDisplayable instance.
 * 
 * @author Pavel DOMECKÝ
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.2.0
 */
public class JSimDetailedInfoWindow extends JDialog implements Observer
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -6974187500683718335L;

	/**
	 * The owner of this dialog.
	 */
	protected JSimMainWindow myParent;

	/**
	 * The object whose characteristics will be displayed in this dialog.
	 */
	protected JSimDisplayable objectInfo;

	private JPanel panel;
	private JButton button;
	private JTable table;
	private DefaultTableModel tableModel;
	private JScrollPane tableScrollPane;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The dialog's window adapter. Disposes the dialog of when closed.
	 */
	class MyWindowAdapter extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			removeMeFromOpenWindows();
			//setVisible(false);
			//dispose();
		} // windowClosing
	} // inner class MyWindowAdapter

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The dialog's action adapter. Disposes the dialog of when the OK button is pressed.
	 */
	class MyActionAdapter implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			removeMeFromOpenWindows();
			setVisible(false);
			dispose();
		} // actionPerformed
	} // inner class MyActionAdapter

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new dialog containing a table and an OK button.
	 * 
	 * @param parent
	 *            The parent window of the dialog, usually the main window.
	 * @param info
	 *            The object to be displayed in the table.
	 */
	public JSimDetailedInfoWindow(JSimMainWindow parent, JSimDisplayable info)
	{
		super(parent, "Details", false);
		myParent = parent;
		objectInfo = info;

		constructorWindowInit();
		constructorSpecificInit();

		pack();

		// Randomize the window's position
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) Math.rint((d.getWidth() - 200) * Math.random());
		int y = (int) Math.rint((d.getHeight() - 150) * Math.random());
		setLocation(x, y);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Adds basic components and listeners to the window.
	 */
	protected void constructorWindowInit()
	{
		panel = new JPanel();
		button = new JButton("OK");
		panel.add(button);
		getContentPane().add(panel, BorderLayout.SOUTH);
		button.addActionListener(new MyActionAdapter());
		addWindowListener(new MyWindowAdapter());
	} // constructorWindowInit

	/**
	 * Creates and adds specific objects to the window, such as a table and its table model.
	 */
	protected void constructorSpecificInit()
	{
		tableModel = new DefaultTableModel();
		String[] columnNames = { "Name", "Value" };
		tableModel.setColumnCount(2);
		tableModel.setColumnIdentifiers(columnNames);

		table = new JTable(tableModel);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		tableScrollPane = new JScrollPane(table);
		getContentPane().add(tableScrollPane, BorderLayout.CENTER);
		// We must call update to fill the table model
		update(null, null);
		table.setPreferredScrollableViewportSize(new Dimension(200, table.getRowCount() * table.getRowHeight()));
	} // constructorSpecificInit

	/**
	 * Updates the table model. The necessary data for the table model are obtained from the getDetailedInformationArray() method.
	 */
	public void update(Observable o, Object arg)
	{
		Collection<JSimPair> collection;
		Iterator<JSimPair> it;
		JSimPair jsp;
		int i = 0;

		collection = objectInfo.getDetailedInformationArray();
		if ((collection != null))
		{
			tableModel.setRowCount(collection.size());

			it = collection.iterator();
			while (it.hasNext())
			{
				jsp = it.next();
				tableModel.setValueAt(jsp.getName(), i, 0);
				tableModel.setValueAt(jsp.getValue(), i, 1);
				i++;
			} // while
		} // if
	} // update

	/**
	 * Removes the displayable object that this info window shows from the list of currently displayed objects of the main GUI windows. You
	 * can use this method in your window or action adapter if you subclass from JSimDetailedInfoWindow. If you subclass directly from
	 * JDialog or a similar class, you must use the main window's removeDisplayableFromInfoWindows() method directly.
	 */
	protected void removeMeFromOpenWindows()
	{
		myParent.removeDisplayableFromOpenInfoWindows(objectInfo);
	} // removeMeFromOpenWindows

} // JSimDetailedInfoWindow

