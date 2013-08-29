/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidContextException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimKernelPanicException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.gui.JSimDetailedInfoWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimDisplayable;
import cz.zcu.fav.kiv.jsim.gui.JSimMainWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimPair;
import java.io.IOException;

/**
 * A JSimSemaphore is a mean of synchronization of two or more J-Sim processes. Semaphores were invented by
 * <a href="http://www.cs.utexas.edu/users/EWD/" target="_blank">Edsger W. Dijkstra</a>, Professor Emeritus of Computer Sciences and
 * Mathematics at The University of Texas. Semaphores typically protect shared data from being accessed concurrently by two or more
 * processes. Semaphore functions P() and V() serve as the beginning and the end of a critical section, preventing any process from passing
 * over P() if another process has already passed over it. When the other process, being currently inside the critical section, invokes V()
 * on the semaphore, it releases the semaphore and the process waiting on P() is woken up and enters the critical section. An integer
 * counter and a queue are used for blocking and resuming processes.
 * 
 * <em>Caution: This implementation of semaphore can be used inside a J-Sim simulation only! It is not a mean of synchronization of Java
 * threads!</em>
 * 
 * Important note: <em>Semaphores can only be used by processes from the same simulation.</em> An attempt to invoke a method of a
 * semaphore of simulation S<sub>1</sub> from a process of simulation S<sub>2</sub> will cause an unpredictable and probably faulty
 * behavior.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimSemaphore implements JSimDisplayable, Comparable<JSimSemaphore>
{
	/**
	 * Common logger for all instances of this class. By default, all logging information goes to a file. Only severe events go to the
	 * console, in addition to a file.
	 */
	private static final Logger logger;

	/**
	 * The name of the semaphore.
	 */
	private final String myName;

	/**
	 * This semaphore's number. Semaphore numbers are unique for a given simulation.
	 */
	private final long myNumber;

	/**
	 * The simulation in which the semaphore is placed. Protected because child classes will possibly want to use it.
	 */
	protected final JSimSimulation myParent;

	/**
	 * A counter of processes that can pass over P() without blocking. This counter is decremented every time a process passes over P()
	 * without blocking and incremented every time V() is invoked and no process is blocked on P().
	 */
	private long counter;

	/**
	 * A queue of processes blocked on this semaphore's P() function.
	 */
	private LinkedList<JSimProcess> queue;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The static block initializes all static attributes.
	 */
	static
	{
		logger = Logger.getLogger(JSimSemaphore.class.getName());
	} // static

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new semaphore with the specified name and initial value of the counter. Every semaphore must belong to a simulation, as
	 * processes and queues. Important note: <em>Semaphores can only be used by processes from the same simulation.</em> An attempt to
	 * invoke a method of a semaphore of simulation S<sub>1</sub> from a process of simulation S<sub>2</sub> will cause an unpredictable
	 * and probably faulty behavior.
	 * 
	 * @param name
	 *            The name of the semaphore.
	 * @param parent
	 *            The parent simulation that this semaphore belongs to.
	 * @param initCounter
	 *            The initial value of the semaphore. It must be non-negative. The initial value determines the number of processes that can
	 *            invoke P() without being blocked. A reasonable value is 1 in most cases. If you want to simulate a critical section, the
	 *            value of 1 is the only choice.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the parent simulation is null or the initial value of the counter is invalid.
	 * @exception JSimTooManySemaphoresException
	 *                This exception is thrown out if no more semaphore can be added to the simulation.
	 */
	public JSimSemaphore(String name, JSimSimulation parent, long initCounter) throws JSimInvalidParametersException, JSimTooManySemaphoresException, IOException
	{
		if (parent == null)
			throw new JSimInvalidParametersException("JSimSemaphore.JSimSemaphore(): parent");
		if (initCounter < 0)
			throw new JSimInvalidParametersException("JSimSemaphore.JSimSemaphore(): initCounter");

		myParent = parent;
		myNumber = myParent.getFreeSemaphoreNumber();
		if (myNumber == JSimSimulation.NEW_SEMAPHORE_FORBIDDEN)
			throw new JSimTooManySemaphoresException("JSimSemaphore.JSimSemaphore()");

		myName = name;
		counter = initCounter;
		queue = new LinkedList<JSimProcess>();
		myParent.addSemaphore(this);
	} // constructor

	/**
	 * P() is a potentially blocking operation that denotes the beginning of a critical section. If the counter of processes using the
	 * semaphore is positive, the calling process passes beyond P(). Otherwise, it is blocked on the semaphore and remains blocked until a
	 * corresponding number of V() invocations is made. Important note:
	 * <em>Semaphores can only be used by processes from the same simulation.</em> An attempt to invoke a method of a semaphore of
	 * simulation S<sub>1</sub> from a process of simulation S<sub>2</sub> will cause an unpredictable and probably faulty behavior.
	 * 
	 * @exception JSimInvalidContextException
	 *                This exception is thrown out if this method is called from outside a process. This means that no process is currently
	 *                running.
	 */
	public void P() throws JSimInvalidContextException
	{
		JSimProcess callingProcess;

		// This is out of the try block to throw the exception out of the method.
		callingProcess = myParent.getRunningProcess();
		if (callingProcess == null)
			throw new JSimInvalidContextException("JSimSemaphore.P(): no running process");

		try
		{
			if (counter > 0)
				counter--;
			else
			{
				queue.addLast(callingProcess);
				callingProcess.blockOnSemaphore(this);
			} // else
		} // try
		catch (JSimException e)
		{
			logger.log(Level.SEVERE, "Blocking the process on a semaphore did not succeed.", e);
			throw new JSimKernelPanicException(e);
		} // catch
	} // P

	/**
	 * V() denotes the end of a critical section. If the queue of waiting processes is empty, the counter is incremented. Otherwise, the
	 * first process waiting in the queue is woken up and can proceed with performing operations inside the critical section. The invocation
	 * of V() need not necessarily follow an invocation of P(). They can be invoked in the inverse order, even from different processes in
	 * some special cases. Important note: <em>Semaphores can only be used by processes from the same simulation.</em> An attempt to
	 * invoke a method of a semaphore of simulation S<sub>1</sub> from a process of simulation S<sub>2</sub> will cause an unpredictable
	 * and probably faulty behavior.
	 * 
	 * @exception JSimInvalidContextException
	 *                This exception is thrown out if this method is called from outside a process. This means that no process is currently
	 *                running.
	 */
	public void V() throws JSimInvalidContextException
	{
		JSimProcess firstProcess;

		// This is out of the try block to throw the exception out of the method.
		if (myParent.getRunningProcess() == null)
			throw new JSimInvalidContextException("JSimSemaphore.V(): no running process");

		try
		{
			if (queue.size() < 1)
				counter++;
			else
			{
				firstProcess = queue.removeFirst();
				firstProcess.unblockFromSemaphore(this);
			} // else
		} // try
		catch (NoSuchElementException e1)
		{
			logger.log(Level.SEVERE, getSemaphoreName() + ": V(): Cannot remove a process from the wait queue.", e1);
			throw new JSimKernelPanicException(e1);
		} // catch
		catch (JSimException e2)
		{
			logger.log(Level.SEVERE, "Unblocking the process from a semaphore did not succeed.", e2);
			throw new JSimKernelPanicException(e2);
		} // catch
	} // V

	/**
	 * Returns the number of processes blocked on this semaphore's P() function.
	 * 
	 * @return The number of processes blocked on this semaphore's P() function.
	 */
	public int getNumberOfWaitingProcesses()
	{
		return queue.size();
	} // getNumberOfWaitingProcesses

	/**
	 * Returns this semaphore's counter.
	 * 
	 * @return This semaphore's counter.
	 */
	public long getCounter()
	{
		return counter;
	} // getCounter

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the semaphore's number.
	 * 
	 * @return The semaphore's number.
	 */
	public final long getSemaphoreNumber()
	{
		return myNumber;
	} // getSemaphoreNumber

	/**
	 * Returns the semaphore's name.
	 * 
	 * @return The semaphore's name.
	 */
	public String getSemaphoreName()
	{
		return myName;
	} // getSemaphoreName

	/**
	 * Returns the simulation that this semaphore is a part of.
	 * 
	 * @return The simulation that this semaphore is a part of.
	 */
	public JSimSimulation getParent()
	{
		return myParent;
	} // getParent

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a string containing basic information about the semaphore. The string can be displayed in a JSimMainWindowList component.
	 * 
	 * @return A string containing basic information about the semaphore.
	 */
	public String getObjectListItemDescription()
	{
		return toString();
	} // getObjectListItemDescription

	/**
	 * Returns a collection with the semaphore's characteristics. Every characteristics contains a name and a value. The collection can be
	 * displayed in a JSimGUIDetailedInfoWindow table.
	 * 
	 * @return A collection of the semaphore's characteristics.
	 */
	public Collection<JSimPair> getDetailedInformationArray()
	{
		Collection<JSimPair> c = new ArrayList<JSimPair>(4);
		c.add(new JSimPair("Number:", Long.toString(getSemaphoreNumber())));
		c.add(new JSimPair("Name:", getSemaphoreName()));
		c.add(new JSimPair("Counter:", Long.toString(getCounter())));
		c.add(new JSimPair("No of waiting processes:", Integer.toString(getNumberOfWaitingProcesses())));
		return c;
	} // getDetailedInformationArray

	/**
	 * Creates a detailed info window that shows information about the semaphore. Returns a reference to the created window.
	 * 
	 * @return A reference to the created info window.
	 */
	public JDialog createDetailedInfoWindow(JSimMainWindow parentWindow)
	{
		JSimDetailedInfoWindow dWindow = new JSimDetailedInfoWindow(parentWindow, this);
		return dWindow;
	} // createDetailedInfoWindow

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a string representation of the semaphore. Provided information: number, name, ...
	 * 
	 * @return A string representation of the semaphore.
	 */
	public String toString()
	{
		return ("No.: " + getSemaphoreNumber() + " Name: " + getSemaphoreName());
	} // toString

	/**
	 * Compares this semaphore with another one. Returns a negative integer, zero, or a positive integer as this semaphore is less than,
	 * equal to, or greater than the specified object. It is assumed that the argument is also a JSimSemaphore. This class has a natural
	 * ordering that is fully consistent with equals(). If equals() returns true for s1 and s2, then compareTo() will return 0 for the same
	 * s1 and s2, and vice versa.
	 * 
	 * @return Zero if the numbers of both semaphores are equal, a negative number if the number of this semaphore is less than the other
	 *         semaphore's number, and a positive number if the number of this semaphore is greater than the other semaphore's number.
	 * 
	 * @exception ClassCastException
	 *                This exception is thrown out when the specified object cannot be typecasted to JSimSemaphore.
	 */
	public int compareTo(JSimSemaphore s)
	{
		if (this.myParent.getSimulationNumber() == s.myParent.getSimulationNumber())
			if (this.myNumber == s.myNumber)
				return 0;
			else
				if (this.myNumber < s.myNumber)
					return -1;
				else
					return +1;
		else
			if (this.myParent.getSimulationNumber() < s.myParent.getSimulationNumber())
				return -1;
			else
				return +1;
	} // compareTo

	/**
	 * Indicates whether some other object is equal to this one. This implementation compares semaphore numbers and their simulations'
	 * numbers which is actually equal to simple reference comparison because semaphore numbers are unique for a given simulation and
	 * simulation numbers are unique for a given JVM instance.
	 * 
	 * Unique semaphore numbers are assured by the constructor and the JSimSimulation.getFreeSemaphoreNumber() method. Unique simulation
	 * numbers are assured by the JSimSimulation constructor.
	 * 
	 * @param o
	 *            The reference object with which to compare.
	 * 
	 * @return True if this object is the same as the obj argument, false otherwise.
	 */
	public boolean equals(Object o)
	{
		if (o == this)
			return true;

		if ((o instanceof JSimSemaphore) == false)
			return false;

		JSimSemaphore s = (JSimSemaphore) o;

		if ((this.myNumber == s.myNumber) && (this.myParent.getSimulationNumber() == s.myParent.getSimulationNumber()))
			return true;
		else
			return false;
	} // equals

	/**
	 * Returns a hash code value for the object. The hash code is computed from the semaphore's number and its simulation's number using the
	 * algorithm described in [UJJ3/166]. This implementation of hash code computation is fully consistent with equals().
	 * 
	 * @return A hash code for this semaphore.
	 */
	public int hashCode()
	{
		int temp = 17; // Magic number 17
		int myNumberAsInt = (int) (myNumber ^ (myNumber >>> 32));
		int simulationNumber = myParent.getSimulationNumber();

		temp = 37 * temp + myNumberAsInt; // Another magic number 37
		temp = 37 * temp + simulationNumber;

		return temp;
	} // hashCode

} // class JSimSemaphore
