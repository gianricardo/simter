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
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cz.zcu.fav.kiv.jsim.JSimCalendar;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimMethodNotSupportedException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSimulationMode;
import cz.zcu.fav.kiv.jsim.JSimSimulationState;
import java.io.IOException;

/**
 * The JSimMainWindow class provides services for graphic output. You should never use this class directly since the JSimSimulation class
 * contains methods for running in graphic mode.
 * 
 * @author Pavel DOMECKÝ
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.1
 */
public class JSimMainWindow extends JFrame
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = -4221191798348575280L;

	/**
	 * Shows the simulation's current time and its changes.
	 */
	class JFieldCurrentTime extends JTextField implements Observer
	{
		/**
		 * Serialization identification.
		 */
		private static final long serialVersionUID = 8207452821306371405L;

		/**
		 * Creates a new JTextField having the specified count of columns.
		 * 
		 * @param columns
		 *            Number of columns.
		 */
		JFieldCurrentTime(int columns)
		{
			super(columns);
		} // constructor

		/**
		 * Rewrites the old time in the text field with the current simulation time.
		 */
		public void update(Observable o, Object arg)
		{
			double newTime;

			newTime = myParent.getCurrentTime();

			if (newTime != JSimCalendar.NEVER)
				fieldCurrentTime.setText(Double.toString(newTime));
			else
				fieldCurrentTime.setText("Simulation terminated.");
		} // update
	} // inner class JFieldCurrentTime

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * An observer responsible for disabling control buttons when the main window is waiting for its closure by the user. Applicable in the
	 * GUI batch mode only.
	 */
	class CommonChanges implements Observer
	{
		/**
		 * Tests whether the window is waiting for manual closure and if it is, disables control buttons used in the GUI batch mode.
		 */
		public void update(Observable o, Object arg)
		{
			if (myParent.getWaitingForWindowClosure())
			{
				buttonContinue.setEnabled(false);
				buttonPause.setEnabled(false);
			} // if
		} // update
	} // inner class CommonChanges

	// ------------------------------------------------------------------------------------------------------------------------------------

	/*
	 * The main window's window adapter. Performs some cleanup when the window is closed.
	 */
	class MyWindowAdapter extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			actionQuit();
		} // windowClosing
	} // inner class MyWindowAdapter

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The main window's action adapter. Performs actions associated with buttons of the main window.
	 */
	class MyActionAdapter implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();

			if (source == buttonQuit)
				actionQuit();
			if (source == buttonOneStep) {
                            try {
                                actionRunOneStep();
                            } 
                            catch (IOException ex) {
                                Logger.getLogger(JSimMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
			if (source == buttonUntil) {
                            try {
                                actionRunUntilTimeLimit();
                            } 
                            catch (IOException ex) {
                                Logger.getLogger(JSimMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
			if (source == buttonNoOfSteps) {
                            try {
                                actionRunNumberOfSteps();
                            } 
                            catch (IOException ex) {
                                Logger.getLogger(JSimMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
			if (source == buttonPause)
				actionPause();
			if (source == buttonContinue)
				actionContinue();
		} // actionPerformed
	} // inner class MyActionAdapter

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The main window's mouse adapter. If the user double clicks on a list, the selected item's detailed info window is opened.
	 */
	class MyMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
				openUpDetailedInfoWindow((JSimMainWindowList) e.getSource());
		} // mouseClicked
	} // inner class MyMouseAdapter

	// ------------------------------------------------------------------------------------------------------------------------------------

	public static final int LIST_TYPE_PROCESS = 1;
	public static final int LIST_TYPE_QUEUE = 2;
	public static final int LIST_TYPE_SEMAPHORE = 3;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Common logger for all instances of this class. By default, all logging information goes to a file. Only severe events go to the
	 * console, in addition to a file.
	 */
	private static final Logger logger;

	/**
	 * Simulation this window belongs to.
	 */
	private JSimSimulation myParent;

	/**
	 * The simulation mode: text, GUI batch, or GUI interactive.
	 */
	private JSimSimulationMode mode;

	/**
	 * The lock used to suspend the main thread while the window is open.
	 */
	private Object graphicLock;

	/**
	 * The lock used to notify the main thread while the simulation is paused in GUI batch mode.
	 */
	private Object stepLock;

	/**
	 * The lock used to notify the main thread while the simulation waits in waitForWindowClosure().
	 */
	private Object endLock;

	/**
	 * The main window's action listener.
	 */
	private MyActionAdapter myActionListener;

	/**
	 * The main window's mouse listener.
	 */
	private MyMouseAdapter myMouseListener;

	/**
	 * The main window's window listener.
	 */
	private MyWindowAdapter myWindowListener;

	// Panels
	private JPanel buttonPanel;
	//private JPanel listPanel;
	private JPanel processesPanel;
	private JPanel queuesPanel;

	// TextFields
	private JTextField fieldUntil;
	private JTextField fieldNoOfSteps;

	// Observers
	private JFieldCurrentTime fieldCurrentTime;
	private CommonChanges commonCh;

	// Buttons
	private JButton buttonQuit;
	private JButton buttonOneStep;
	private JButton buttonUntil;
	private JButton buttonNoOfSteps;
	private JButton buttonPause;
	private JButton buttonContinue;

	// SplitPanes
	private JSplitPane splitPane;
	private JSplitPane splitListPane;

	// ScrollPanes
	private JScrollPane userOutputScrollPane;
	private JScrollPane processListScrollPane;
	private JScrollPane queueListScrollPane;

	// Lists
	private JSimMainWindowList processList;
	private JSimMainWindowList queueList;

	// TextArea
	private JTextArea userOutput;

	/**
	 * Flag saying whether it is possible to close the window.
	 */
	private boolean canClose;

	/**
	 * Flag saying whether the quit button has been pressed.
	 */
	private boolean quitPressed;

	/**
	 * Flag saying whether the pause button has been pressed.
	 */
	private boolean paused;

	/**
	 * The main dispatcher of changes generated during simulation progress. Used to notify various GUI components.
	 */
	private JSimChange guiUpdate;

	/**
	 * A list of open detailed info windows.
	 */
	private ArrayList<JSimDisplayable> openDetailedWindows;

	/**
	 * The new-line property of the OS used.
	 */
	private String newLine;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The static block initializes all static attributes.
	 */
	static
	{
		logger = Logger.getLogger(JSimMainWindow.class.getName());
	} // static

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new main window with several buttons, input fields and a text area. Appearance of the window depends on the simulation's
	 * mode.
	 * 
	 * @param simulation
	 *            The simulation object that owns this newly created window. It must not be null.
	 * @param simMode
	 *            The mode in which the simulation runs, design of the window depends on it.
	 * 
	 * @exception JSimSimulationAlreadyTerminatedException
	 *                This exception is thrown out if the simulation has already terminated.
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the simulation is null or one of its locks cannot be obtained.
	 */
	public JSimMainWindow(JSimSimulation simulation, JSimSimulationMode simMode) throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException
	{
		super("J-Sim Main Window");

		if (simulation == null)
			throw new JSimInvalidParametersException("JSimMainWindow.JSimMainWindow(): simulation");

		if (simulation.getSimulationState() == JSimSimulationState.TERMINATED)
			throw new JSimSimulationAlreadyTerminatedException("JSimMainWindow.JSimMainWindow()");

		myParent = simulation;
		mode = simMode;

		graphicLock = myParent.getGraphicLock();
		if (graphicLock == null)
			throw new JSimInvalidParametersException("JSimMainWindow.JSimMainWindow(): simulation[graphicLock]");

		stepLock = myParent.getStepLock();
		if (stepLock == null)
			throw new JSimInvalidParametersException("JSimMainWindow.JSimMainWindow(): simulation[stepLock]");

		endLock = myParent.getWindowClosureLock();
		if (endLock == null)
			throw new JSimInvalidParametersException("JSimMainWindow.JSimMainWindow(): simulation[endLock]");

		canClose = true;
		quitPressed = false;
		paused = false;

		guiUpdate = myParent.getChange();
		commonCh = new CommonChanges();
		openDetailedWindows = new ArrayList<JSimDisplayable>();
		newLine = System.getProperty("line.separator");

		// Initializations for both graphic modes
		myActionListener = new MyActionAdapter();
		myMouseListener = new MyMouseAdapter();
		myWindowListener = new MyWindowAdapter();
		addWindowListener(myWindowListener);

		setFont(new Font("SansSerif", Font.PLAIN, 12));

		buttonPanel = new JPanel();
		buttonQuit = new JButton("Quit");
		fieldCurrentTime = new JFieldCurrentTime(10);
		fieldCurrentTime.setText(Double.toString(myParent.getCurrentTime()));
		fieldCurrentTime.setEditable(false);
		guiUpdate.addObserver(fieldCurrentTime);

		userOutput = new JTextArea(25, 58);
		userOutput.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		userOutputScrollPane = new JScrollPane(userOutput);

		// Process list and its scrollpane
		processList = new JSimMainWindowList(myParent, LIST_TYPE_PROCESS);
		guiUpdate.addObserver(processList);
		processList.addMouseListener(myMouseListener);
		processListScrollPane = new JScrollPane(processList);

		// Queue list and its scrollpane
		queueList = new JSimMainWindowList(myParent, LIST_TYPE_QUEUE);
		guiUpdate.addObserver(queueList);
		queueList.addMouseListener(myMouseListener);
		queueListScrollPane = new JScrollPane(queueList);

		// Processes panel = label + processListScrollPane
		processesPanel = new JPanel();
		processesPanel.setLayout(new BoxLayout(processesPanel, BoxLayout.Y_AXIS));
		processesPanel.add(new JLabel("Processes:", JLabel.CENTER));
		processesPanel.add(processListScrollPane);

		// Queues panel = label + queueListScrollPane
		queuesPanel = new JPanel();
		queuesPanel.setLayout(new BoxLayout(queuesPanel, BoxLayout.Y_AXIS));
		queuesPanel.add(new JLabel("Queues:", JLabel.CENTER));
		queuesPanel.add(queueListScrollPane);

		// splitListPane divides processesPanel and queuesPanel
		splitListPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, processesPanel, queuesPanel);
		splitListPane.setDividerLocation(210);
		splitListPane.setOneTouchExpandable(true);

		// splitPane divides userOutputScrollPane and splitListPane
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userOutputScrollPane, splitListPane);
		splitPane.setDividerLocation(0.8);
		splitPane.setOneTouchExpandable(true);

		// Initializations specific for each simulation mode
		switch (mode)
		{
			case GUI_BATCH:
				buttonPanel.setLayout(new GridLayout(1, 5));

				buttonPause = new JButton("Pause");
				buttonPause.setToolTipText("Suspends the simulation");

				buttonContinue = new JButton("Continue");
				buttonContinue.setEnabled(false);
				buttonContinue.setToolTipText("Continues the simulation");

				buttonPanel.add(new JLabel("Current Time:"));
				buttonPanel.add(fieldCurrentTime);

				buttonPanel.add(buttonPause);
				buttonPause.addActionListener(myActionListener);

				buttonPanel.add(buttonContinue);
				buttonContinue.addActionListener(myActionListener);

				buttonPanel.add(buttonQuit);
				buttonQuit.addActionListener(myActionListener);

				guiUpdate.addObserver(commonCh);
				paused = false;
				break;

			case GUI_INTERACTIVE:
				buttonPanel.setLayout(new GridLayout(2, 6));
				buttonOneStep = new JButton("Run one step");
				buttonUntil = new JButton("Run until the time limit");
				buttonNoOfSteps = new JButton("Run N steps");

				fieldUntil = new JTextField(10);
				fieldUntil.setText(Double.toString(myParent.getCurrentTime()));
				fieldNoOfSteps = new JTextField(5);
				fieldNoOfSteps.setText("5");

				buttonPanel.add(new JLabel("Current Time:", JLabel.CENTER));
				buttonPanel.add(new JLabel("Time Limit:", JLabel.RIGHT));

				buttonPanel.add(fieldUntil);
				buttonPanel.add(buttonUntil);
				buttonUntil.addActionListener(myActionListener);
				buttonPanel.add(buttonQuit);
				buttonQuit.addActionListener(myActionListener);
				buttonPanel.add(fieldCurrentTime);
				buttonPanel.add(new JLabel("Number of steps to run:", JLabel.RIGHT));
				buttonPanel.add(fieldNoOfSteps);
				buttonPanel.add(buttonNoOfSteps);
				buttonNoOfSteps.addActionListener(myActionListener);
				buttonPanel.add(buttonOneStep);
				buttonOneStep.addActionListener(myActionListener);
				break;

			default:
				logger.log(Level.WARNING, "Main GUI window: Unknown GUI mode specified!");
				break;
		} // switch

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);

		setTitle("J-Sim: " + myParent.getSimulationName());
		this.pack();
		setLocationRelativeTo(null);
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the simulation object to which this window belongs to.
	 * 
	 * @return The simulation object to which this window belongs to.
	 */
	public JSimSimulation getSimulation()
	{
		return myParent;
	} // getSimulation

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Closes the window, notifies the main thread and returns control to the simulation. Called when the `Quit' button is pressed.
	 */
	private synchronized void actionQuit()
	{
		switch (mode)
		{
			case GUI_BATCH:
				myParent.windowIsClosing(this);
				logger.log(Level.FINE, "The main simulation window is closing up.");
				quitPressed = true;
				// If we are waiting in pause we must let step() exit first. The step will not be executed
				// because step() detects that the button has been pressed after it is woken up.
				if (paused)
				{
					paused = false;
					synchronized (stepLock)
					{
						stepLock.notify();
					} // synchronized (stepLock)
				} // if paused

				// Destroy the window
				setVisible(false);
				dispose();
				logger.log(Level.FINE, "Main window destroyed.");

				// If we are waiting in waitForWindowClosure() we must let it exit.
				if (myParent.getWaitingForWindowClosure())
				{
					synchronized (endLock)
					{
						endLock.notify();
					} // synchronized (endLock)
				} // if waiting for closure
				break;

			case GUI_INTERACTIVE:
				synchronized (graphicLock)
				{
					// If the simulation is running we cannot close the window.
					if (!canClose)
						JOptionPane.showMessageDialog(null, "Please wait until the currently running operation finishes and then try again.", "Closing the window", JOptionPane.ERROR_MESSAGE);
					else
					{
						myParent.windowIsClosing(this);
						logger.log(Level.FINE, "The main simulation window is closing up.");
						graphicLock.notify();
						setVisible(false);
					} // else cannot close
				} // synchronized (graphicLock)
				break;

			default:
				logger.log(Level.WARNING, "Main GUI window: Unknown GUI mode specified! Cleanup not done properly.");
				break;
		} // switch mode
	} // actionQuit

	/**
	 * Runs one simulation step. Called when the `Run one step' button is pressed.
	 */
	private void actionRunOneStep() throws IOException
	{
		boolean result;

		// We needn't check canClose here because this button is disabled when an action is being performed.
		canClose = false;
		result = true;
		buttonQuit.setEnabled(false);
		disableRunButtons();

		// Let's do one step now
		try
		{
			result = myParent.step();
		} // try
		catch (JSimMethodNotSupportedException e) // This should never happen.
		{
			logger.log(Level.WARNING, "The simulation refused to execute a step.", e);
		} // catch

		// If the simulation has already terminated, we must inform the user and let buttons disabled.
		if (result == false)
			JOptionPane.showMessageDialog(null, "The simulation has terminated.", "J-Sim", JOptionPane.INFORMATION_MESSAGE);
		else
			enableRunButtons();

		// Always enable the quit button
		buttonQuit.setEnabled(true);
		canClose = true;
	} // actionRunOneStep

	/**
	 * Runs an unspecified number of simulation steps until the simulation time is equal to or greater than the time specified in the `Time
	 * Limit' input field. Called when the `Run until the time limit' button is pressed.
	 */
	private void actionRunUntilTimeLimit() throws IOException
	{
		boolean result;
		double endTime = 0.0;
		boolean inputOK = true;

		// Reading the number is a potentially dangerous operation.
		try
		{
			endTime = Double.parseDouble(fieldUntil.getText());
		}
		catch (NumberFormatException e)
		{
			inputOK = false;
		}

		if (endTime <= myParent.getCurrentTime())
			inputOK = false;

		if (inputOK)
		{
			// We needn't check canClose here because this button is disabled when an action is being performed.
			canClose = false;
			buttonQuit.setEnabled(false);
			disableRunButtons();

			// Now we are doing as steps until we reach the desired time (or get over it).
			// But if the simulation terminates we break up the cycle, of course.
			result = true;
			try
			{
				while ((myParent.getCurrentTime() < endTime) && (result == true))
					result = myParent.step();
			} // try
			catch (JSimMethodNotSupportedException e) // This should never happen.
			{
				logger.log(Level.WARNING, "The simulation refused to execute a step.", e);
			} // catch

			// If the simulation has already terminated, we must inform the user and let buttons disabled.
			if (result == false)
				JOptionPane.showMessageDialog(null, "The simulation has terminated.", "J-Sim", JOptionPane.INFORMATION_MESSAGE);
			else
				enableRunButtons();

			// Always enable the quit button
			buttonQuit.setEnabled(true);
			canClose = true;
		} // input OK
		else
			JOptionPane.showMessageDialog(null, "The value entered is invalid or less than the current time. Please correct and try again.", "J-Sim", JOptionPane.ERROR_MESSAGE);
	} // actionRunUntilTimeLimit

	/**
	 * Runs the number of steps specified in the `Number of steps to run' input field. Called when the `Run N steps' button is pressed.
	 */
	private void actionRunNumberOfSteps() throws IOException
	{
		boolean result;
		int howManySteps = 0;
		boolean inputOK = true;
		int i;

		// Reading the number is a potentially dangerous operation.
		try
		{
			howManySteps = Integer.parseInt(fieldNoOfSteps.getText());
		} // try
		catch (NumberFormatException e)
		{
			inputOK = false;
		} // catch

		if (howManySteps < 1)
			inputOK = false;

		if (inputOK)
		{
			// We needn't check canClose here because this button is disabled when an action is being performed.
			canClose = false;
			buttonQuit.setEnabled(false);
			disableRunButtons();

			// Now we are doing as many steps as was required.
			// But if the simulation terminates we break up the cycle, of course.
			result = true;

			try
			{
                            for (i = 0; (i < howManySteps) && (result == true); i++) {
                                result = myParent.step();
                            }
			} // try
			catch (JSimMethodNotSupportedException e) // This should never happen.
			{
				logger.log(Level.WARNING, "The simulation refused to execute a step.", e);
			} // catch

			// If the simulation has already terminated, we must inform the user and let buttons disabled.
			if (result == false)
				JOptionPane.showMessageDialog(null, "The simulation has terminated.", "J-Sim", JOptionPane.INFORMATION_MESSAGE);
			else
				enableRunButtons();

			// Always enable the quit button.
			buttonQuit.setEnabled(true);
			canClose = true;
		} // input OK
		else
			JOptionPane.showMessageDialog(null, "The value entered is invalid. Please correct and try again.", "J-Sim", JOptionPane.ERROR_MESSAGE);
	} // actionRunNumberOfSteps

	/**
	 * Suspends the simulation. Called when the `Pause' button is pressed. The step() method will blocked after this method is invoked.
	 */
	private void actionPause()
	{
		paused = true;
		buttonPause.setEnabled(false);
		buttonContinue.setEnabled(true);
	} // actionPause

	/**
	 * Unblocks the simulation. Called when the `Continue' button is pressed. The step() method will be unblocked and it will continue.
	 */
	private void actionContinue()
	{
		paused = false;
		buttonPause.setEnabled(true);
		buttonContinue.setEnabled(false);

		synchronized (stepLock)
		{
			stepLock.notify();
		} // synchronized(stepLock)
	} // actionContinue

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns true if the 'Quit' button has been pressed.
	 * 
	 * @return True if the 'Quit' button has been pressed, false otherwise.
	 */
	public synchronized final boolean getQuitPressed()
	{
		return quitPressed;
	} // getQuitPressed

	/**
	 * Returns true if the 'Pause' button has been pressed.
	 * 
	 * @return True if the 'Pause' button has been pressed, false otherwise.
	 */
	public synchronized final boolean getPausePressed()
	{
		return paused;
	} // getPausePressed

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Prints out a string to the user text area at the bottom of the window.
	 * 
	 * @param s
	 *            The string to be printed out.
	 * @param code
	 *            A code saying whether it is an info message (PRN_MESSAGE) or an error message (PRN_ERROR).
	 * @param appendNewLine
	 *            A flag indicating that the new line character should be appended to the text.
	 */
	public void printString(String s, int code, boolean appendNewLine)
	{
		if (appendNewLine)
			userOutput.append(s + newLine);
		else
			userOutput.append(s);

		// Set the caret behind the last char, supplyies scrolling of the userOutput
		userOutput.setCaretPosition(userOutput.getText().length());

		if (code == JSimSimulation.PRN_ERROR)
			Toolkit.getDefaultToolkit().beep();
	} // printString

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates and displays a detailed info window that displays detailed characteristics of the object corresponding to the selected item
	 * from a list placed inside the main window.
	 * 
	 * @param list
	 *            The list from which the item is selected.
	 */
	private void openUpDetailedInfoWindow(JSimMainWindowList list)
	{
		int selectedIndex = -1;
		JSimDisplayable jsd;
		JDialog detailedWindow;

		try
		{
			if (!list.isSelectionEmpty())
			{
				selectedIndex = list.getSelectedIndex();
				jsd = list.getInfoTable().get(selectedIndex);
				if (jsd != null)
					if (!openDetailedWindows.contains(jsd))
					{
						openDetailedWindows.add(jsd);
						detailedWindow = jsd.createDetailedInfoWindow(this);
						guiUpdate.addObserver((Observer) detailedWindow);
						detailedWindow.setVisible(true);
					} // if window not open yet
			} // if selection not empty
		} // try
		catch (IndexOutOfBoundsException e)
		{
			logger.log(Level.WARNING, "Unable to open a detailed info window for list index " + selectedIndex, e);
		} // catch
	} // openUpDetailedInfoWindow

	/**
	 * When a detailed info window shuts up, it should invoke its main window's removeDisplayableFromInfoWindows() method in order to inform
	 * it that its displayable object is no longer showing. This will allow the main window to open a new detailed info window in the future
	 * if the user requests it.
	 * 
	 * @param jsd
	 *            The JSimDisplayable object whose detailed info window is just shutting up.
	 */
	public void removeDisplayableFromOpenInfoWindows(JSimDisplayable jsd)
	{
		openDetailedWindows.remove(jsd);
	} // closeDetailedInfoWindow

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Disables all buttons except of the `Quit' button.
	 */
	private void disableRunButtons()
	{
		buttonUntil.setEnabled(false);
		buttonOneStep.setEnabled(false);
		buttonNoOfSteps.setEnabled(false);
	} // disableRunButtons

	/**
	 * Enables all buttons except of the `Quit' button.
	 */
	private void enableRunButtons()
	{
		buttonUntil.setEnabled(true);
		buttonOneStep.setEnabled(true);
		buttonNoOfSteps.setEnabled(true);
	} // enableRunButtons

} // class JSimMainWindow
