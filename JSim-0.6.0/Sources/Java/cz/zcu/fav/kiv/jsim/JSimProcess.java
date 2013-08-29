/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */
package cz.zcu.fav.kiv.jsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;

import cz.zcu.fav.kiv.jsim.gui.JSimDetailedInfoWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimDisplayable;
import cz.zcu.fav.kiv.jsim.gui.JSimMainWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimPair;
import cz.zcu.fav.kiv.jsim.ipc.JSimMessage;
import cz.zcu.fav.kiv.jsim.ipc.JSimMessageBox;
import cz.zcu.fav.kiv.jsim.ipc.JSimMessageForReceiver;
import cz.zcu.fav.kiv.jsim.ipc.JSimSemaphore;
import cz.zcu.fav.kiv.jsim.ipc.JSimSymmetricMessage;
import java.io.IOException;

/**
 * A JSimProcess is an independent unit of code, having its own data. Its code
 * is placed in the life() method. Processes have possibilities to schedule
 * themselves as well as other processes, they can also cancel another process's
 * schedule. A process must belong to a simulation. During a simulation
 * execution, processes are interleaved in a pseudo-parallel manner, i.e. just
 * one process is running at the same time. When the currently running process
 * invokes hold(), passivate(), or reactivate(), it is suspended and another
 * process can be executed instead.
 *
 * @author Jarda KAČER
 * @author Pavel DOMECKÝ
 *
 * @version J-Sim version 0.6.0
 *
 * @since J-Sim version 0.0.1
 */
public class JSimProcess extends Thread implements JSimDisplayable, Comparable<JSimProcess> {

    /**
     * Methods passivate(), hold(), and reactivate() will ignore first
     * INT_REQUESTS_TO_IGNORE requests to warn the programmer.
     */
    public static final int INT_REQUESTS_TO_IGNORE = 1;
    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Common logger for all instances of this class. By default, all logging
     * information goes to a file. Only severe events go to the console, in
     * addition to a file.
     */
    private static final Logger logger;
    /**
     * The process's number. It is unique for all process within the same
     * simulation.
     */
    private final long myNumber;
    /**
     * The process's name.
     */
    private final String myName;
    /**
     * This process's state &ndash; new, passive, scheduled, active, blocked on
     * semaphore, terminated, etc.
     */
    private JSimProcessState myState;
    /**
     * The simulation that this process is a part of. Protected because child
     * classes will want to use it.
     */
    protected final JSimSimulation myParent;
    /**
     * Lock used when switching between the simulation and the process. Final
     * &ndash; can be just created, not changed afterwards.
     */
    private final Object myOwnLock;
    /**
     * Flag saying whether this process has been started. Even a new thread can
     * be scheduled, we must distinguish between being new and being started.
     */
    private boolean haveIBeenStarted;
    /**
     * Flag saying whether this process has been interrupted by its simulation
     * and should terminate.
     */
    private boolean shouldTerminate;
    /**
     * Number of terminating warnings this process has got since it received an
     * interrupt signal. It should be at most 1.
     */
    private int requestsToTerminate;
    /**
     * The simulation time that the process is scheduled for (if it is
     * scheduled).
     */
    private double scheduleTime;
    /**
     * The semaphore that this process is currently blocked on, if it is blocked
     * at all.
     */
    private JSimSemaphore semaphoreIAmCurrentlyBlockedOn;
    /**
     * Every process has its own message box where messages sent directly
     * between processes are stored.
     */
    private JSimMessageBox myMessageBox;
    /**
     * The message clipboard is a single message passed from a process to this
     * process when this process has invoked a blocking receive operation and
     * got blocked inside it.
     */
    private JSimMessage messageClipboard;
    /**
     * A process that this process is waiting for. This process will stay
     * suspended inside a blocking receive operation until the sender sends it a
     * message.
     */
    private JSimProcess messageSenderIAmWaitingFor;

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * The static block initializes all static attributes.
     */
    static {
        logger = Logger.getLogger(JSimProcess.class.getName());
    } // static

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new process having a name and belonging to a simulation. If the
     * simulation is already terminated or there is no free number the process
     * is not created and an exception is thrown out. All processes must have
     * their parent simulation object specified. If not, an exception is thrown,
     * too. After the creation, the process is not scheduled and therefore will
     * not run unless explicitely activated by another process or the main
     * program using activate().
     *
     * @param name Name of the process being created.
     * @param parent Parent simulation.
     *
     * @exception JSimSimulationAlreadyTerminatedException This exception is
     * thrown out if the simulation has already been terminated.
     * @exception JSimInvalidParametersException This exception is thrown out if
     * no parent was specified.
     * @exception JSimTooManyProcessesException This exception is thrown out if
     * no other process can be added to the simulation specified.
     * @exception JSimKernelPanicException This exception is thrown out if the
     * simulation is in a unknown state. Do NOT catch this exception!
     */
    public JSimProcess(String name, JSimSimulation parent) throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name);

        // We must know our simulation context
        if (parent == null) {
            throw new JSimInvalidParametersException("JSimProcess.JSimProcess(): parent");
        } else {
            myParent = parent;
        }

        myNumber = myParent.getFreeProcessNumber();
        if (myNumber == JSimSimulation.NEW_PROCESS_FORBIDDEN) {
            throw new JSimTooManyProcessesException("JSimProcess.JSimProcess()");
        }
        myName = name;

        // Final -- Must be initialized before branching
        myOwnLock = new Object();

        // Sometimes, it is not allowed to create a new process. Let's see...
        switch (myParent.getSimulationState()) {
            // OK, let's create a new J-Sim process
            case NOT_STARTED:
            case IN_PROGRESS:
                try {
                    myState = JSimProcessState.NEW;
                    myParent.addProcess(this);
                    haveIBeenStarted = false;
                    shouldTerminate = false;
                    requestsToTerminate = 0;
                    scheduleTime = JSimCalendar.NEVER;
                    semaphoreIAmCurrentlyBlockedOn = null;
                    myMessageBox = new JSimMessageBox("Message Box for " + name);
                    messageClipboard = null;
                    messageSenderIAmWaitingFor = null;
                } // try
                catch (JSimInvalidParametersException e) {
                    logger.log(Level.WARNING, "Invalid parameters when creating a process.", e);
                    throw e; // Rethrow the exception.
                } // catch
                break;

            // Notify the user by throwing out an exception
            case TERMINATED:
                throw new JSimSimulationAlreadyTerminatedException(name);

            // Something strange must have happened. Ufff...
            default:
                logger.log(Level.SEVERE, "NEW PROCESS: The simulation is in an unknown state. J-Sim Kernel in panic. Please report.");
                throw new JSimKernelPanicException();
        } // switch
    } // constructor

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Stops the process and waits until it is woken up by a signal. When a new
     * process is started, it always blocks here and waits until the main thread
     * switches to it when its turn comes. You should never use this method. It
     * is called automatically from run().
     *
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    protected void getReady() {
        synchronized (myOwnLock) {
            try // outer try
            {
                // The process might have been already scheduled with activate().
                // In that case, let it be so. Otherwise, change the state from NEW to PASSIVE.
                if (getProcessState() != JSimProcessState.SCHEDULED) {
                    setProcessState(JSimProcessState.PASSIVE);
                }

                while (myParent.getRunningProcess() != this) {
                    try // inner try
                    {
                        myOwnLock.wait();
                    } // inner try
                    catch (InterruptedException e1) {
                        // We must know this not to enter the life() method.
                        // The simulation is shutting down.
                        shouldTerminate = true;
                        requestsToTerminate++;

                        // We must not fall asleep again. When the simulation deletes all
                        // threads, the running process is set to NOBODY.
                        // Without break, we would sleep forever because every process is
                        // interrupted just once during the shutdown.
                        break;
                    } // catch
                } // while

                // If we have to die this exception is propagated till run().
                // Otherwise we will normally continue with life().
                if (shouldTerminate) {
                    logger.log(Level.FINE, getName() + " GET_READY: Killed, never run.");
                    throw new JSimProcessDeath();
                } // if
                else {
                    setProcessState(JSimProcessState.ACTIVE);
                }
            } // outer try
            catch (JSimInvalidProcessStateException e2) {
                logger.log(Level.WARNING, getName() + " GET_READY: A bug appeared. Expect simulation malfunction.", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
        } // synchronized
    } // getReady

    /**
     * Informs the parent simulation about its termination and switches back to
     * the main thread. You should never use this method.
     */
    private final void finish() {
        synchronized (myOwnLock) {
            try {
                // If we are not killed but just have run to the end we must inform our parent
                if (!shouldTerminate) {
                    myParent.deleteProcess(this);
                    myParent.switchToNobody();
                    myOwnLock.notify();
                    logger.log(Level.FINE, getName() + " FINISH: Dying and switching to the main thread.");
                } // if
                else {
                    // If we are killed by our parent, everything is OK; we are not doing anything.
                    // The simulation has done everything in shutdown().
                    logger.log(Level.FINE, getName() + " FINISH: Killed by the main thread, no activity.");
                } // else

                setProcessState(JSimProcessState.TERMINATED);
                // And now, we can peacefully die...
            } // try
            catch (JSimInvalidProcessStateException e) {
                logger.log(Level.WARNING, "Invalid parameters when dying.", e);
            } // catch
        } // synchronized(myOwnLock)
    } // finish

    /**
     * The process's life. Should be always overwritten in order to execute a
     * meaningful action sequence. Must NOT be synchronized!
     */
    protected void life() {
        message(getName() + ": You should overwrite the `life()' method, sir. Read the documentation, please.");
    } // life

    /**
     * This method should never be overwritten or called directly. It call
     * getReady(), life() and finish() and handles some extraordinary
     * situations. You should never use or modify this method (it is final).
     *
     * Should be PRIVATE, but can't be &ndash; the parent's rights are public.
     */
    public final void run() {
        try {
            getReady();
            if (!shouldTerminate) {
                life();
            }
        } // try
        catch (JSimProcessDeath death) {
            shouldTerminate = true; // This should have been set already
            logger.log(Level.FINE, getName() + " RUN: Process interrupted while sleeping.");
        } // catch
        // Catching all runtime exceptions here prevents J-Sim from hanhing up. We must run the finish()
        // method to delete the process from the system as if it finished his life normally.
        // Therefore, `shouldTerminate' must be set to false in order to switch to the main thread.
        catch (RuntimeException re) {
            shouldTerminate = false;
            logger.log(Level.WARNING, "J-Sim WARNING: You have a bug in your source code!");
            logger.log(Level.WARNING, "J-Sim WARNING: Your process `" + getName() + "' has just crashed down.");
            logger.log(Level.WARNING, "J-Sim WARNING: The process is going to be removed from the simulation.");
            logger.log(Level.WARNING, "Here is the error description:", re);
        } // catch
        finish();
    } // run

    /**
     * Starts the process. You should never use this method. All processes are
     * started automatically when the step() method of the simulation is
     * invoked.
     */
    public void start() {
        super.start();
        haveIBeenStarted = true;
    } // start

    /**
     * Returns true if the process has been started already. All processes are
     * started automatically when the step() method of the simulation is
     * invoked. You should never use this method.
     *
     * @return True if the process has been started already, false otherwise.
     */
    protected synchronized final boolean hasBeenStarted() {
        return haveIBeenStarted;
    } // hasBeenStarted

    /**
     * Returns the process's private lock that the simulation uses for switching
     * between itself and the process. The lock never changes. You should never
     * use this method.
     *
     * @return The process's private lock.
     */
    public Object getPrivateLock() {
        return myOwnLock;
    } // getPrivateLock

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the process's number. The number is unique within the whole
     * simulation.
     *
     * @return The process's number.
     */
    public final long getProcessNumber() {
        return myNumber;
    } // getProcessNum

    /**
     * Returns the process's name.
     *
     * @return The process's name.
     */
    public String getProcessName() {
        return myName;
    } // getProcessName

    /**
     * Returns the simulation that this process is a part of.
     *
     * @return The simulation that this process is a part of.
     */
    public JSimSimulation getParent() {
        return myParent;
    } // getParent

    /**
     * Returns the process's state. Possible values are new, passive, scheduled,
     * active, and terminated.
     *
     * @return The process's state.
     */
    public synchronized final JSimProcessState getProcessState() {
        return myState;
    } // getProcessState

    /**
     * Returns the time for which is the process scheduled. If the process is
     * not scheduled, JSimCalendar.NEVER is returned.
     *
     * @return Time for which is the process scheduled or JSimCalendar.NEVER.
     */
    public final double getScheduleTime() {
        return scheduleTime;
    } // getProcessNum

    /**
     * Switches the process to a new state. You should never use this method
     * since this is an internal one. You should have a very serious reason to
     * override it.
     *
     * @param newState A new state that the process has to be switched to.
     *
     * @exception JSimInvalidProcessStateException This exception is thrown out
     * if the process is not allowed to change its state from the current one to
     * the new one.
     */
    protected synchronized void setProcessState(JSimProcessState newState) throws JSimInvalidProcessStateException {
        String errorLocation = "JSimProcess.setProcessState(): newState";
        boolean canSwitch = false;

        switch (myState) {
            case NEW:
                // NEW -> PASSIVE:    getReady() without activation before being started
                // NEW -> SCHEDULED:  activate() before being started (between its creation and next step())
                // NEW -> TERMINATED: simulation shutdown without the step() method being called after this process creation
                if ((newState == JSimProcessState.PASSIVE) || (newState == JSimProcessState.SCHEDULED) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // NEW

            case PASSIVE:
                // PASSIVE -> SCHEDULED:  activate() called
                // PASSIVE -> TERMINATED: simulation shutdown when sleeping in passivate() or getReady() -- terminated via JSimProcessDeath
                if ((newState == JSimProcessState.SCHEDULED) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // PASSIVE

            case SCHEDULED:
                // SCHEDULED -> PASSIVE:    cancel() invoked
                // SCHEDULED -> ACTIVE:     picked up from the calendar and restarted from getReady(), hold(), passivate(), or reactivate()
                // SCHEDULED -> TERMINATED: simulation shutdown when sleeping in hold() -- terminated via JSimProcessDeath
                if ((newState == JSimProcessState.PASSIVE) || (newState == JSimProcessState.ACTIVE) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // SCHEDULED

            case ACTIVE:
                // ACTIVE -> PASSIVE:                      passivate() invoked
                // ACTIVE -> SCHEDULED:                    hold() invoked
                // ACTIVE -> BLOCKED_ON_SEMAPHORE:         blockOnSemaphore() invoked via a semaphore's P()
                // ACTIVE -> BLOCKED_ON_MESSAGE_SEND:      sendMessageWithBlocking()
                // ACTIVE -> BLOCKED_ON_MESSAGE_RECEIVE:   receiveMessageWithBlocking()
                // ACTIVE -> TERMINATED:                   natural death -- switched in finish()
                if ((newState == JSimProcessState.PASSIVE)
                        || (newState == JSimProcessState.SCHEDULED)
                        || (newState == JSimProcessState.BLOCKED_ON_SEMAPHORE)
                        || (newState == JSimProcessState.TERMINATED)
                        || (newState == JSimProcessState.BLOCKED_ON_MESSAGE_SEND)
                        || (newState == JSimProcessState.BLOCKED_ON_MESSAGE_RECEIVE)) {
                    canSwitch = true;
                }
                break; // ACTIVE

            case BLOCKED_ON_SEMAPHORE:
                // BLOCKED_ON_SEMAPHORE -> SCHEDULED:  unblockFromSemaphore() invoked via a semaphore's V()
                // BLOCKED_ON_SEMAPHORE -> TERMINATED: simulation shutdown when sleeping in blockOnSemaphore() -- terminated via JSimProcessDeath
                if ((newState == JSimProcessState.SCHEDULED) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // BLOCKED_ON_SEMAPHORE

            case BLOCKED_ON_MESSAGE_SEND:
                // BLOCKED_ON_MESSAGE_SEND -> SCHEDULED:  another process invokes receiveMessageXXX() and the sender/receiver matches
                // BLOCKED_ON_MESSAGE_SEND -> TERMINATED: simulation shutdown when sleeping in sendMessageWithBlocking() -- terminated via JSimProcessDeath
                if ((newState == JSimProcessState.SCHEDULED) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // BLOCKED_ON_MESSAGE_SEND

            case BLOCKED_ON_MESSAGE_RECEIVE:
                // BLOCKED_ON_MESSAGE_RECEIVE -> SCHEDULED:  another process invokes sendMessageXXX() and the sender/receiver matches
                // BLOCKED_ON_MESSAGE_RECEIVE -> TERMINATED: simulation shutdown when sleeping in receiveMessageWithBlocking() -- terminated via JSimProcessDeath
                if ((newState == JSimProcessState.SCHEDULED) || (newState == JSimProcessState.TERMINATED)) {
                    canSwitch = true;
                }
                break; // BLOCKED_ON_MESSAGE_RECEIVE

            // Do not do anything in TERMINATED
            case TERMINATED:
                break;

            // Do not do anything in an unknown state
            default:
                break;
        } // switch

        if (canSwitch == true) {
            myState = newState;
        } else {
            throw new JSimInvalidProcessStateException(errorLocation, myState, newState);
        }
    } // setProcessState

    /**
     * Returns true if the process is idle, false otherwise. A process is idle
     * if it is not currently running and can be freely activated. Actually, the
     * process is idle if it is in the passive or new state.
     *
     * @return True if the process is idle, false otherwise.
     */
    public boolean isIdle() {
        JSimProcessState state;

        state = getProcessState();
        if ((state == JSimProcessState.NEW) || (state == JSimProcessState.PASSIVE)) {
            return true;
        } else {
            return false;
        }
    } // isIdle

    /**
     * Returns the process's state as string.
     *
     * @return The process's state as string.
     */
    public String getProcessStateAsString() {
        return getProcessState().toString();
    } // getProcessStateAsString

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * The main switching routine, responsible for switching between the main
     * simulation thread and the process and back. When called, it wakes the
     * main thread up and passivates itself. Then it waits passivated until the
     * main thread performing the simulation's step() method sends a signal to
     * the process via notify(). <em>You should never call this method
     * directly.</em> It is used internally by J-Sim in methods like
     * passivate(), hold(), or any other method blocking the currently active
     * simulation process.
     *
     * @param requestedBlockingState The process will be set to this state
     * during its passivation. Pay attention! The setState() method must allow
     * to set the new state!
     * @param callingMethodName The name of the method calling the switching
     * routine.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be switched over is not currently active or if another process
     * calls this process's method. Note: The two conditions should be
     * equivalent since there can be at most one active J-Sim process at one
     * time.
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. <em>You should NEVER catch it!</em>
     */
    protected final void mainSwitchingRoutine(JSimProcessState requestedBlockingState, String callingMethodName) throws JSimSecurityException {
        // It is not allowed to switch to the main thread if this J-Sim process is not active.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.mainSwitchingRoutine(): This process is not active.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.mainSwitchingRoutine(): This process is not the only running process of its simulation.");
        }

        synchronized (myOwnLock) {
            try // outer try
            {
                if ((shouldTerminate) && (requestsToTerminate >= INT_REQUESTS_TO_IGNORE)) {
                    logger.log(Level.WARNING, "J-Sim WARNING: You probably suppress J-Sim's mechanisms of process handling.");
                    logger.log(Level.WARNING, "J-Sim WARNING: This is very dangerous and may lead to deadlock.");
                    logger.log(Level.WARNING, "J-Sim WARNING: Please check up your source code unless you really know what you are doing.");
                } // if

                // Wake up main() which is now waiting in step().
                myParent.switchToNobody();
                myOwnLock.notify();

                // Going to sleep now.
                // The new state is a state in which the process is passive or waiting for an event:
                //	* passive
                //	* blocked on semaphore
                //	* blocked on message send
                //	* blocked on message receive
                setProcessState(requestedBlockingState);
                while (myParent.getRunningProcess() != this) {
                    try // inner try
                    {
                        if (shouldTerminate) {
                            requestsToTerminate++;
                        }
                        myOwnLock.wait();
                    } // inner try
                    catch (InterruptedException e1) {
                        shouldTerminate = true;
                        requestsToTerminate++;
                        // Why `break' is used?
                        // We must get out of the cycle and throw out the exception.
                        // We can't simply test (!shouldTerminate) in the cycle header
                        // because we would not ever sleep again.
                        // This means the current process would run without any hold()s
                        // or passivate()s to infinity if the user caught JSimDeath in
                        // his life() and then invoked passive() or hold() again.
                        break;
                    } // catch
                } // while not me

                // If we have to die, this exception is propagated till run().
                if (shouldTerminate) {
                    logger.log(Level.INFO, getName() + " " + callingMethodName + ": Killed.");
                    throw new JSimProcessDeath();
                } // if
                else {
                    setProcessState(JSimProcessState.ACTIVE);
                    scheduleTime = JSimCalendar.NEVER;
                } // else
            } // outer try
            catch (JSimInvalidProcessStateException e2) {
                logger.log(Level.SEVERE, "Wrong process states in the main switching routine.", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
            // We are now normally returning to the calling method and then to life().
        } // synchronized
    } // mainSwitchingRoutine

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Passivates the process. Returns the control back to the parent simulation
     * and does not add any new event to the calendar. A process cannot
     * passivate another process, only itself.
     *
     * Will be called from MyProcess.life() --&gt; protected because MyProcess
     * will extend JSimProcess. Still `a' can call `b.passivate()' but it is
     * taken care about.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be passivated is not currently active or if another process
     * calls this process's passivate() method. Note: The two conditions should
     * be equivalent.
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    protected final void passivate() throws JSimSecurityException {
        // It is not allowed to passivate somebody else.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.passivate(): Only an active process can be passivated.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.passivate(): The process being called is not running just now.");
        }

        mainSwitchingRoutine(JSimProcessState.PASSIVE, "Passivate");
    } // passivate

    /**
     * Passivates the process but adds a new entry to the calendar, specifying
     * the time when the process will be activated again. A process cannot call
     * another process's hold(), only its own.
     *
     * Will be called from MyProcess.life() --&gt; protected because MyProcess
     * will extend JSimProcess. Still `a' can call `b.hold()' but it is taken
     * care about.
     *
     * @param deltaT The time delta specifying the difference between the
     * current time and the time when the process will be activated.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be hold is not currently active or if another process calls
     * this process's hold() method. Note: The two conditions should be
     * equivalent.
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the time difference is negative. Processes cannot be scheduled to the
     * past.
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    protected final void hold(double deltaT) throws JSimSecurityException, JSimInvalidParametersException {
        // It is not allowed to hold somebody else.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.hold(): Only an active process can be hold.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.hold(): The process being called is not running just now.");
        }
        if (deltaT < 0.0) {
            throw new JSimInvalidParametersException("JSimProcess.hold(): The delta must be non-negative.");
        }

        try {
            synchronized (myOwnLock) {
                scheduleTime = myParent.getCurrentTime() + deltaT;
                myParent.addEntryToCalendar(scheduleTime, this);
            } // synchronized

            mainSwitchingRoutine(JSimProcessState.SCHEDULED, "Hold");
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "Wrong process states in hold().", e);
            throw new JSimKernelPanicException(e); // deltaT is handled already, so nothing should normally happen.
        } // catch
    } // hold

    /**
     * Activates a process at the given time. Adds a new entry to the calendar,
     * specifying the time when the process will be activated. A process cannot
     * activate itself but it can activate another process.
     *
     * Will be called from MySim.main() or MyProcess.life() --&gt; public
     * because MySim will not be in the same package.
     *
     * @param when Absolute simulation time when the process should be
     * activated.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be activated is not currently passive or new.
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the time of activation is less than current simulation time.
     */
    public final void activate(double when) throws JSimSecurityException, JSimInvalidParametersException {
        if (!((getProcessState() == JSimProcessState.NEW) || (getProcessState() == JSimProcessState.PASSIVE))) {
            throw new JSimSecurityException("JSimProcess.activate(): Only a passive or new process can be activated.");
        }

        synchronized (myOwnLock) {
            if (when >= myParent.getCurrentTime()) {
                try {
                    myParent.addEntryToCalendar(when, this);
                    scheduleTime = when;
                    setProcessState(JSimProcessState.SCHEDULED);
                } // try
                catch (JSimInvalidParametersException e1) // from addCalendarEntry
                {
                    logger.log(Level.SEVERE, "Invalid parameters, cannot add new calendar event.", e1);
                    throw new JSimKernelPanicException(e1);
                } // catch
                catch (JSimInvalidProcessStateException e2) // from setProcessState
                {
                    logger.log(Level.SEVERE, "Wrong process states in activate().", e2);
                    throw new JSimKernelPanicException(e2);
                } // catch
            } // if when >= current time
            else {
                throw new JSimInvalidParametersException("JSimProcess.activate(): when (less than current simulation time)");
            }
        } // synchronized
    } // activate

    /**
     * Activates a process at the current simulation time. Adds a new entry to
     * the calendar, with time set to now. A process cannot activate itself but
     * it can activate another process. There is no guarantee that the activated
     * process will run in the next step. It can be run later if there are more
     * processes scheduled for the same time.
     *
     * Will be called from MySim.main() or MyProcess.life() --&gt; public
     * because MySim will not be in the same package.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be activated is not currently passive or new.
     */
    public final void activateNow() throws JSimSecurityException {
        if (!((getProcessState() == JSimProcessState.NEW) || (getProcessState() == JSimProcessState.PASSIVE))) {
            throw new JSimSecurityException("JSimProcess.activateNow(): Only a passive or new process can be activated.");
        }

        synchronized (myOwnLock) {
            try {
                double when = myParent.getCurrentTime();

                myParent.addEntryToCalendar(when, this);
                scheduleTime = when;
                setProcessState(JSimProcessState.SCHEDULED);
            } // try
            catch (JSimInvalidParametersException e1) // from addCalendarEntry
            {
                logger.log(Level.SEVERE, "Invalid parameters, cannot add new calendar event.", e1);
                throw new JSimKernelPanicException(e1);
            } // catch
            catch (JSimInvalidProcessStateException e2) // from setProcessState
            {
                logger.log(Level.SEVERE, "Wrong process states in activateNow().", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
        } // synchronized
    } // activateNow

    /**
     * Deletes all process's scheduled events from the calendar. The process
     * will not be run anymore unless activated again by another process. A
     * process can cancel another process but not itself. Will be called from
     * MyProcess.life() --&gt; public because it need not necessarily be in the
     * same package as the called process.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be cancelled is not currently scheduled or if the process
     * calls its own cancel() method. (Note: The latter condition is a subset of
     * the former one because the currently running process can never be
     * scheduled.)
     */
    public final void cancel() throws JSimSecurityException {
        // A process can only cancel a another process that is scheduled.
        if (getProcessState() != JSimProcessState.SCHEDULED) {
            throw new JSimSecurityException("JSimProcess.cancel(): Only a scheduled process can be cancelled.");
        }
        if (myParent.getRunningProcess() == this) {
            throw new JSimSecurityException("JSimProcess.cancel(): You may not cancel yourself!");
        }

        synchronized (myOwnLock) {
            try {
                myParent.deleteEntriesInCalendar(this, true);
                setProcessState(JSimProcessState.PASSIVE);
                scheduleTime = JSimCalendar.NEVER;
            } // try
            catch (JSimInvalidParametersException e1) // from deleteEntriesInCalendar
            {
                logger.log(Level.SEVERE, "Cannot delete calendar events.", e1);
                throw new JSimKernelPanicException(e1);
            } // catch
            catch (JSimInvalidProcessStateException e2) // from setProcessState
            {
                logger.log(Level.SEVERE, "Wrong process states in cancel().", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
        } // synchronized
    } // cancel

    /**
     * Reactivates the process. The process is temporarily suspended as with
     * passivate() but an event is added to the calendar for the same simulation
     * time. This means that the process will run again in a future step (not
     * necessarily the next one) and the value of the simulation time will be
     * the same as now. The purpose of this method is to let other processes run
     * if they have been scheduled for the same simulation time. Since later
     * scheduled processes are run later (if the simulation times are equal)
     * this process will run after all processes already scheduled for the
     * current simulation time.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be reactivated is not currently active or if another process
     * calls this process's reactivate() method. Note: The two conditions should
     * be equivalent.
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    protected final void reactivate() throws JSimSecurityException {
        // It is not allowed to reactivate somebody else.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.reactivate(): Only an active process can be reactivated.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.reactivate(): The process being called is not running just now.");
        }

        try {
            hold(0.0);
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "Process reactivation failed.", e);
            throw new JSimKernelPanicException(e);
        } // catch
    } // reactivate

    /**
     * Passivates the process and inserts it to the specified queue. The process
     * can be later taken out from the queue and reactivated using activate().
     *
     * @param head The queue that the process has to be inserted to.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the queue to which the process should be inserted is null.
     * @exception JSimSecurityException This exception is thrown out when the
     * process of which wait() method is called is not currently active or if
     * another process calls this process's wait() method. (Note: The two
     * conditions should be equivalent.)
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    protected final void wait(JSimHead head) throws JSimSecurityException, JSimInvalidParametersException {
        JSimLink link;

        // It is not allowed to invoke this on somebody else.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.wait(): Only an active process can be suspended.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.wait(): The process being called is not running just now.");
        }
        if (head == null) {
            throw new JSimInvalidParametersException("JSimProcess.wait(): head");
        }

        try {
            link = new JSimLink(this);
            link.into(head);
            passivate();
        } // try
        catch (JSimException e) {
            logger.log(Level.SEVERE, "Process could not be enqueued and passivated.", e);
            throw new JSimKernelPanicException(e);
        } // catch
    } // wait

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Blocks the process until it is unblocked by a semaphore's V() function.
     * Returns the control back to the parent simulation and does not add any
     * new event to the calendar. A process cannot block another process, only
     * itself via a semaphore's P() function. Blocking on a semaphore terminates
     * the current simulation step.
     *
     * @param semaphore The semaphore on which P() function this process is
     * getting blocked.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be blocked is not currently active or if another process calls
     * this process's blockOnSemaphore() method. (Note: The two conditions
     * should be equivalent.)
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the semaphore in not specified (is null) or if its parent is not this
     * process's parent.
     * @exception JSimProcessDeath This internal J-Sim exception is thrown out
     * when the simulation is shutting down. You should NEVER catch it!
     */
    public final void blockOnSemaphore(JSimSemaphore semaphore) throws JSimSecurityException, JSimInvalidParametersException {
        // It is not allowed to block somebody else.
        if (getProcessState() != JSimProcessState.ACTIVE) {
            throw new JSimSecurityException("JSimProcess.blockOnSemaphore(): Only an active process can be blocked.");
        }
        if (myParent.getRunningProcess() != this) {
            throw new JSimSecurityException("JSimProcess.blockOnSemaphore(): You may not do this!");
        }
        if (semaphore == null) {
            throw new JSimInvalidParametersException("JSimProcess.blockOnSemaphore(): semaphore");
        }
        if (semaphore.getParent() != myParent) {
            throw new JSimInvalidParametersException("JSimProcess.blockOnSemaphore(): semaphore (different parents)");
        }

        synchronized (myOwnLock) {
            semaphoreIAmCurrentlyBlockedOn = semaphore;
        } // synchronized

        mainSwitchingRoutine(JSimProcessState.BLOCKED_ON_SEMAPHORE, "BlockOnSemaphore");
    } // blockOnSemaphore

    /**
     * Unblocks the process which has previously got blocked on a semaphore.
     * Adds a new entry to the calendar with the current time. The process does
     * not start running immediately but switches to the scheduled state and
     * waits until it is re-started by the simulation from its step() method.
     *
     * @param semaphore The semaphore that this process is currently blocked on.
     *
     * @exception JSimSecurityException This exception is thrown out when the
     * process to be unblocked is not currently blocked.
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the semaphore is null or its parent is not this process's parent or it is
     * not the semaphore that this process is blocked on.
     */
    public final void unblockFromSemaphore(JSimSemaphore semaphore) throws JSimSecurityException, JSimInvalidParametersException {
        if (getProcessState() != JSimProcessState.BLOCKED_ON_SEMAPHORE) {
            throw new JSimSecurityException("JSimProcess.unblockFromSemaphore(): Only a blocked process can be unblocked.");
        }
        if (semaphore == null) {
            throw new JSimInvalidParametersException("JSimProcess.unblockFromSemaphore(): semaphore");
        }
        if (semaphore.getParent() != myParent) {
            throw new JSimInvalidParametersException("JSimProcess.blockOnSemaphore(): semaphore (different parents)");
        }
        if (semaphore != semaphoreIAmCurrentlyBlockedOn) {
            throw new JSimInvalidParametersException("JSimProcess.blockOnSemaphore(): This process is not blocked on the semaphore specified.");
        }

        synchronized (myOwnLock) {
            try {
                double currentTime = myParent.getCurrentTime();
                myParent.addEntryToCalendar(currentTime, this);
                scheduleTime = currentTime;
                setProcessState(JSimProcessState.SCHEDULED);
                semaphoreIAmCurrentlyBlockedOn = null;
            } // try
            catch (JSimInvalidParametersException e1) // from addCalendarEntry
            {
                logger.log(Level.SEVERE, "Cannot schedule an unblocked process.", e1);
                throw new JSimKernelPanicException(e1);
            } // catch
            catch (JSimInvalidProcessStateException e2) // from setProcessState
            {
                logger.log(Level.SEVERE, "Cannot set an unblocked process's state.", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
        } // synchronized
    } // unblockFromSemaphore

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the process's message box. The box is used for storing messages
     * coming directly to the process.
     *
     * @return The process's message box.
     */
    protected JSimMessageBox getMessageBox() {
        return myMessageBox;
    } // getMessageBox

    /**
     * Indicates whether this process's clipboard is empty or not. The clipboard
     * is used to store a single message taken out from a message box when the
     * process is blocked during a blocking send operation.
     *
     * @return True is this process's clipboard is empty, false otherwise.
     */
    public boolean hasEmptyMessageClipboard() {
        return (messageClipboard == null);
    } // hasEmptyMessageClipboard

    /**
     * Copies a single message to the process's clipboard. The clipboard must be
     * empty. No physical copying is done, just reference assignment.
     *
     * @param message The message to be put into the clipboard.
     *
     * @exception JSimSecurityException This exception is thrown out if the
     * clipboard is not empty.
     */
    protected void copyToMessageClipboard(JSimMessage message) throws JSimSecurityException {
        if (!hasEmptyMessageClipboard()) {
            throw new JSimSecurityException("The clipboard is full!");
        }

        messageClipboard = message;
    } // copyToMessageClipboard

    /**
     * Reads a message from the clipboard and returns it. If there is no message
     * in the clipboard, null is returned. The message is taken out from the
     * clipboard.
     *
     * @return A message stored in the clipboard or null.
     */
    protected JSimMessage readFromClipboard() {
        JSimMessage message;

        message = messageClipboard;
        messageClipboard = null;

        return message;
    } // readFromClipboard

    /**
     * Returns the process that this process specified as sender for its
     * blocking receive operation. If no sender was specified or this process is
     * not performing a blocking receive operation, null is returned.
     *
     * @return The process that this process specified as sender for its
     * blocking receive operation.
     */
    public JSimProcess getSenderIAmWaitingFor() {
        return messageSenderIAmWaitingFor;
    } // getSenderIAmWaitingFor

    /**
     * A routine used for process reactivation after a successfully completed
     * send or receive blocking operation.
     *
     * @exception JSimSecurityException This exception is thrown out if the
     * process is not currently blocked.
     */
    private final void unblockFromMessageSendOrReceive() throws JSimSecurityException {
        if ((getProcessState() != JSimProcessState.BLOCKED_ON_MESSAGE_SEND) && (getProcessState() != JSimProcessState.BLOCKED_ON_MESSAGE_RECEIVE)) {
            throw new JSimSecurityException("JSimProcess.unblockFromMessageSendOrReceive(): Only a blocked process can be unblocked.");
        }

        synchronized (myOwnLock) {
            try {
                double currentTime = myParent.getCurrentTime();
                myParent.addEntryToCalendar(currentTime, this);
                scheduleTime = currentTime;
                setProcessState(JSimProcessState.SCHEDULED);
            } // try
            catch (JSimInvalidParametersException e1) // from addCalendarEntry
            {
                logger.log(Level.SEVERE, "Cannot schedule an unblocked process.", e1);
                throw new JSimKernelPanicException(e1);
            } // catch
            catch (JSimInvalidProcessStateException e2) // from setProcessState
            {
                logger.log(Level.SEVERE, "Cannot set an unblocked process's state.", e2);
                throw new JSimKernelPanicException(e2);
            } // catch
        } // synchronized
    } // unblockFromMessageSendOrReceive

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Sends the specified message to the process denoted as receiver of the
     * message. If the receiver is not currently waiting for the message, the
     * sending process will block and the current simulation step will be
     * terminated.
     *
     * @param message The message to be sent.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message is not specified (null).
     */
    public void sendMessageWithBlocking(JSimSymmetricMessage message) throws JSimInvalidParametersException {
        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithBlocking(): message");
        }

        sendMessageWithBlocking(message, message.getReceiver().getMessageBox());
    } // sendMessageWithBlocking

    /**
     * Sends the specified message to the process denoted as receiver of the
     * message. If the receiver is not currently waiting for the message, the
     * sending process will block and the current simulation step will be
     * terminated.
     *
     * @param message The message to be sent.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message is not specified (null).
     */
    public void sendMessageWithBlocking(JSimMessageForReceiver message) throws JSimInvalidParametersException {
        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithBlocking(): message");
        }

        sendMessageWithBlocking(message, message.getReceiver().getMessageBox());
    } // sendMessageWithBlocking

    /**
     * Sends the specified message to the specified message box. If there is no
     * receiver currently waiting for the message, the sending process will
     * block and the current simulation step will be terminated.
     *
     * @param message The message to be sent.
     * @param box The message box that the message has to be put to.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message or the the message box is not specified (null).
     */
    public void sendMessageWithBlocking(JSimMessage message, JSimMessageBox box) throws JSimInvalidParametersException {
        JSimProcess sendingProcess;
        JSimProcess suspendedReceiver;

        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithBlocking(): message");
        }
        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithBlocking(): box");
        }

        try {
            sendingProcess = this;
            message.setRealSender(sendingProcess);
            // Is there a suspended process that is waiting for this message?
            // If there is one, give it the message directly, otherwise put the message to the box and suspend itself.
            // A process with full clipboard cannot be returned.
            // The sendingProcess argument has usually no value when receivers wait for messages from anybody.
            suspendedReceiver = box.getFirstSuspendedReceiver(message.getReceiver(), sendingProcess);
            if (suspendedReceiver == null) {
                box.addMessage(message);
                box.addSuspendedSender(sendingProcess);
                logger.log(Level.FINE, getName() + ": Blocking send -- No suspended receiver found for receiver=`" + message.getReceiver() + "' and sender=`" + sendingProcess + "'.");
                mainSwitchingRoutine(JSimProcessState.BLOCKED_ON_MESSAGE_SEND, "SendMessageWithBlocking");
            } // if no suspended receiver
            else {
                suspendedReceiver.copyToMessageClipboard(message);
                suspendedReceiver.unblockFromMessageSendOrReceive();
            } // else no suspended receiver
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.SEVERE, "A serious error occured during message send.", e);
            throw new JSimKernelPanicException(e);
        } // catch
    } // sendMessageWithBlocking

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Sends the specified message to the process denoted as receiver of the
     * message. If the receiver is not currently waiting for the message, the
     * sending process will just store the message to its message box. In any
     * case, the sender returns immediately.
     *
     * @param message The message to be sent.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message is not specified (null).
     */
    public void sendMessageWithoutBlocking(JSimSymmetricMessage message) throws JSimInvalidParametersException {
        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithoutBlocking(): message");
        }

        sendMessageWithoutBlocking(message, message.getReceiver().getMessageBox());
    } // sendMessageWithoutBlocking

    /**
     * Sends the specified message to the process denoted as receiver of the
     * message. If the receiver is not currently waiting for the message, the
     * sending process will just store the message to its message box. In any
     * case, the sender returns immediately.
     *
     * @param message The message to be sent.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message is not specified (null).
     */
    public void sendMessageWithoutBlocking(JSimMessageForReceiver message) throws JSimInvalidParametersException {
        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithoutBlocking(): message");
        }

        sendMessageWithoutBlocking(message, message.getReceiver().getMessageBox());
    } // sendMessageWithoutBlocking

    /**
     * Sends the specified message to the specified message box. If there is no
     * receiver currently waiting for the message, the sending process will just
     * store the message to the message box. In any case, the sender returns
     * immediately.
     *
     * @param message The message to be sent.
     * @param box The message box that the message has to be put to.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message or the the message box is not specified (null).
     */
    public void sendMessageWithoutBlocking(JSimMessage message, JSimMessageBox box) throws JSimInvalidParametersException {
        JSimProcess sendingProcess;
        JSimProcess suspendedReceiver;

        if (message == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithoutBlocking(): message");
        }
        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.sendMessageWithoutBlocking(): box");
        }

        try {
            sendingProcess = this;
            message.setRealSender(sendingProcess);
            // Is there a suspended process that is waiting for this message?
            // If there is one, give it the message directly, otherwise put the message to the box.
            // A process with full clipboard cannot be returned.
            if ((suspendedReceiver = box.getFirstSuspendedReceiver(message.getReceiver(), sendingProcess)) == null) {
                box.addMessage(message);
            } else {
                suspendedReceiver.copyToMessageClipboard(message);
                suspendedReceiver.unblockFromMessageSendOrReceive();
            } // else no suspended receiver
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.SEVERE, "A serious error occured during message send.", e);
            throw new JSimKernelPanicException(e);
        } // catch
    } // sendMessageWithoutBlocking

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Receives and returns a message previously sent to this process by any
     * other process. If there is no message in the receiver's message box
     * currently waiting for the receiver, the receiver blocks and the current
     * simulation step is terminated.
     *
     * @return A message read from the receiver's message box.
     */
    public JSimMessage receiveMessageWithBlocking() {
        try {
            return receiveMessageWithBlocking(getMessageBox(), JSimMessage.UNKNOWN_SENDER);
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "An invalid message box provided by a process.", e);
            // The exception is thrown out because of an invalid message box --> J-Sim problem.
            throw new JSimKernelPanicException(e);
        } // catch
    } // receiveMessageWithBlocking

    /**
     * Receives and returns a message from the specified message box. If there
     * is no message in the message box currently waiting for the receiver, the
     * receiver blocks and the current simulation step is terminated.
     *
     * @param box The message box that the message has to be read from.
     *
     * @return A message read from the specified message box previously sent by
     * the specified sender.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message box is not specified.
     */
    public JSimMessage receiveMessageWithBlocking(JSimMessageBox box) throws JSimInvalidParametersException {
        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.receiveMessageWithBlocking(): box");
        }

        // Just ignoring JSimInvalidParametersException and passing it to the user who supplied the box.
        // The exception can be thrown out just because of an invalid message box.
        return receiveMessageWithBlocking(box, JSimMessage.UNKNOWN_SENDER);
    } // receiveMessageWithBlocking

    /**
     * Receives and returns a message previously sent by the specified sender to
     * this process. The sender need not be specified. If it is not specified
     * (JSimMessage.UNKNOWN_SENDER), message from any sender can be returned. If
     * there is no message in the message box of the receiver, currently waiting
     * for the receiver and sent by the sender, the receiver blocks and the
     * current simulation step is terminated.
     *
     * @param sender The sender of the message. Need not be specified.
     *
     * @return A message read from the receiver's message box previously sent by
     * the specified sender.
     */
    public JSimMessage receiveMessageWithBlocking(JSimProcess sender) {
        try {
            return receiveMessageWithBlocking(getMessageBox(), sender);
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "An invalid message box provided by a process.", e);
            // The exception is thrown out because of an invalid message box --> J-Sim problem.
            throw new JSimKernelPanicException(e);
        } // catch
    } // receiveMessageWithBlocking

    /**
     * Receives and returns a message from the specified message box previously
     * sent by the specified sender. The sender need not be specified. If it is
     * not specified (JSimMessage.UNKNOWN_SENDER) message from any sender can be
     * returned. If there is no message in the message box currently waiting for
     * the receiver and sent by the sender, the receiver blocks and the current
     * simulation step is terminated.
     *
     * @param box The message box that the message has to be read from.
     * @param sender The sender of the message. Need not be specified.
     *
     * @return A message read from the specified message box previously sent by
     * the specified sender.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message box is not specified.
     */
    public JSimMessage receiveMessageWithBlocking(JSimMessageBox box, JSimProcess sender) throws JSimInvalidParametersException {
        JSimMessage message = null;
        JSimProcess realSender;

        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.receiveMessageWithBlocking(): box");
        }

        try {
            // If there is no message in the box for this process from the specified sender (or anybody),
            // suspend itself, otherwise take from the box and activate the sender, if it is suspended.
            if ((message = box.getFirstMessageFromAndFor(sender, this)) != null) {
                realSender = message.getRealSender();
                if (box.containsSuspendedSender(realSender)) {
                    realSender.unblockFromMessageSendOrReceive();
                }
            } // if message for me
            else {
                logger.log(Level.FINE, getName() + ": Blocking receive -- No appropriate message found for receiver=`" + this + "' in message box=`" + box + "'.");
                box.addSuspendedReceiver(this);
                mainSwitchingRoutine(JSimProcessState.BLOCKED_ON_MESSAGE_RECEIVE, "ReceiveMessageWithBlocking");
                // Now I am woken up and there is a message from a sender in my clipboard.
                message = readFromClipboard();
            } // else message for me
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.SEVERE, "An error occured during message receive.", e);
            throw new JSimKernelPanicException(e);
        } // catch

        return message;
    } // receiveMessageWithBlocking

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Receives and returns a message from the receiver's message box previously
     * sent by any process. If there is no message currently waiting for the
     * receiver, the method returns null. In any case, the receiver returns
     * immediately from this method.
     *
     * @return A message read from the receiver's message box.
     */
    public JSimMessage receiveMessageWithoutBlocking() {
        try {
            return receiveMessageWithoutBlocking(getMessageBox(), JSimMessage.UNKNOWN_SENDER);
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "An invalid message box provided by a process.", e);
            // The exception is thrown out because of an invalid message box --> J-Sim problem.
            throw new JSimKernelPanicException(e);
        } // catch
    } // receiveMessageWithoutBlocking

    /**
     * Receives and returns a message from the specified message box previously
     * sent by any process. If there is no message in the message box currently
     * waiting for the receiver, the method returns null. In any case, the
     * receiver returns immediately from this method.
     *
     * @param box The message box that the message has to be read from.
     *
     * @return A message read from the specified message box.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message box is not specified.
     */
    public JSimMessage receiveMessageWithoutBlocking(JSimMessageBox box) throws JSimInvalidParametersException {
        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.receiveMessageWithoutBlocking(): box");
        }

        // Just ignoring JSimInvalidParametersException and passing it to the user who supplied the box.
        // The exception can be thrown out just because of an invalid message box.
        return receiveMessageWithoutBlocking(box, JSimMessage.UNKNOWN_SENDER);
    } // receiveMessageWithoutBlocking

    /**
     * Receives and returns a message from the receiver's message box previously
     * sent by the specified sender. The sender need not be specified. If it is
     * not specified (JSimMessage.UNKNOWN_SENDER) message from any sender can be
     * returned. If there is no message in the message box currently waiting for
     * the receiver and sent by the sender, the method returns null. In any
     * case, the receiver returns immediately from this method.
     *
     * @param sender The sender of the message. Need not be specified.
     *
     * @return A message read from the receiver's message box previously sent by
     * the specified sender.
     */
    public JSimMessage receiveMessageWithoutBlocking(JSimProcess sender) {
        try {
            return receiveMessageWithoutBlocking(getMessageBox(), sender);
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.SEVERE, "An invalid message box provided by a process.", e);
            // The exception is thrown out because of an invalid message box --> J-Sim problem.
            throw new JSimKernelPanicException(e);
        } // catch
    } // receiveMessageWithoutBlocking

    /**
     * Receives and returns a message from the specified message box previously
     * sent by the specified sender. The sender need not be specified. If it is
     * not specified (JSimMessage.UNKNOWN_SENDER) message from any sender can be
     * returned. If there is no message in the message box currently waiting for
     * the receiver and sent by the sender, the method returns null. In any
     * case, the receiver returns immediately from this method.
     *
     * @param box The message box that the message has to be read from.
     * @param sender The sender of the message. Need not be specified.
     *
     * @return A message read from the specified message box previously sent by
     * the specified sender.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the message box is not specified.
     */
    public JSimMessage receiveMessageWithoutBlocking(JSimMessageBox box, JSimProcess sender) throws JSimInvalidParametersException {
        JSimMessage message = null;
        JSimProcess realSender;

        if (box == null) {
            throw new JSimInvalidParametersException("JSimProcess.receiveMessageWithoutBlocking(): box");
        }

        try {
            // If there is no message in the box for this process from the specified sender (or anybody),
            // return null, otherwise take from the box and activate the sender, if it is suspended.
            if ((message = box.getFirstMessageFromAndFor(sender, this)) != null) {
                realSender = message.getRealSender();
                if (box.containsSuspendedSender(realSender)) {
                    realSender.unblockFromMessageSendOrReceive();
                }
            } // if message for me
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.SEVERE, "An error occured during message receive.", e);
            throw new JSimKernelPanicException(e);
        } // catch

        return message;
    } // receiveMessageWithoutBlocking

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Prints out a text info message, either to the standard output or to the
     * simulation window.
     *
     * @param s The message to be printed out.
     */
    public void message(String s) {
        myParent.printString(s, JSimSimulation.PRN_MESSAGE, true);
    } // message

    /**
     * Prints a text info message, either to the standard output or to the
     * simulation window, but does not teminate the line.
     *
     * @param s The message to be printed out.
     */
    public void messageNoNL(String s) {
        myParent.printString(s, JSimSimulation.PRN_MESSAGE, false);
    } // message

    /**
     * Prints out a text error message, either to the error output or to the
     * simulation window.
     *
     * @param s The error message to be printed out.
     */
    public void error(String s) {
        myParent.printString(s, JSimSimulation.PRN_ERROR, true);
    } // error

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns a string containing basic information about the process. The
     * string can be displayed in a JSimMainWindowList component.
     *
     * @return A string containing basic information about the process.
     */
    public String getObjectListItemDescription() {
        return toString();
    } // getObjectListItemDescription

    /**
     * Returns a collection of process's characteristics. Every characteristics
     * contains a name and a value. The collection can be displayed in a
     * JSimDetailedInfoWindow table.
     *
     * @return A collection of JSimPairs.
     */
    public Collection<JSimPair> getDetailedInformationArray() {
        Collection<JSimPair> c = new ArrayList<JSimPair>(4);
        c.add(new JSimPair("Number:", Long.toString(getProcessNumber())));
        c.add(new JSimPair("Name:", getName()));
        c.add(new JSimPair("State:", getProcessStateAsString()));
        if (myState == JSimProcessState.SCHEDULED) {
            c.add(new JSimPair("Scheduled for:", Double.toString(scheduleTime)));
        }
        if (myState == JSimProcessState.BLOCKED_ON_SEMAPHORE) {
            c.add(new JSimPair("Semaphore:", semaphoreIAmCurrentlyBlockedOn.getSemaphoreName()));
        }
        return c;
    } // getDetailedInformationArray

    /**
     * Creates a detailed info window that shows information about this process.
     * Returns a reference to the created window.
     *
     * @return Reference to the created info window.
     */
    public JDialog createDetailedInfoWindow(JSimMainWindow parentWindow) {
        JSimDetailedInfoWindow dWindow = new JSimDetailedInfoWindow(parentWindow, this);
        return dWindow;
    } // createDetailedInfoWindow

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns a string representation of the process. Provided information:
     * number, name, state, and schedule time.
     *
     * @return A string representation of the process.
     */
    public String toString() {
        String s = "No.: " + getProcessNumber() + " Name: " + getName() + " State: " + getProcessStateAsString();

        if (getProcessState() == JSimProcessState.SCHEDULED) {
            s = s + " for " + getScheduleTime();
        }
        if (getProcessState() == JSimProcessState.BLOCKED_ON_SEMAPHORE) {
            s = s + " " + semaphoreIAmCurrentlyBlockedOn.getSemaphoreName();
        }

        return s;
    } // toString

    /**
     * Compares this process with another one. Returns a negative integer, zero,
     * or a positive integer as this process is less than, equal to, or greater
     * than the specified object. It is assumed that the argument is also a
     * JSimProcess. This class has a natural ordering that is fully consistent
     * with equals(). If equals() returns true for p1 and p2, then compareTo()
     * will return 0 for the same p1 and p2, and vice versa.
     *
     * @return Zero if the numbers of both processes are equal, a negative
     * number if the number of this process is less than the other process's
     * number, and a positive number if the number of this process is greater
     * than the other process's number.
     *
     * @exception ClassCastException This exception is thrown out when the
     * specified object cannot be typecasted to JSimProcess.
     */
    public int compareTo(JSimProcess p) {
        if (this.myParent.getSimulationNumber() == p.myParent.getSimulationNumber()) {
            if (this.myNumber == p.myNumber) {
                return 0;
            } else if (this.myNumber < p.myNumber) {
                return -1;
            } else {
                return +1;
            }
        } else if (this.myParent.getSimulationNumber() < p.myParent.getSimulationNumber()) {
            return -1;
        } else {
            return +1;
        }
    } // compareTo

    /**
     * Indicates whether some other object is equal to this one. This
     * implementation compares process numbers and their simulations' numbers
     * which is actually equal to simple reference comparison because process
     * numbers are unique for a given simulation and simulation numbers are
     * unique for a given JVM instance. Unique process numbers are assured by
     * the constructor and the JSimSimulation.getFreeProcessNumber() method.
     * Unique simulation numbers are assured by the JSimSimulation constructor.
     *
     * @param o The reference object with which to compare.
     *
     * @return True if this object is the same as the obj argument, false
     * otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if ((o instanceof JSimProcess) == false) {
            return false;
        }

        JSimProcess p = (JSimProcess) o;

        if ((this.myNumber == p.myNumber) && (this.myParent.getSimulationNumber() == p.myParent.getSimulationNumber())) {
            return true;
        } else {
            return false;
        }
    } // equals

    /**
     * Returns a hash code value for the object. The hash code is computed from
     * the process's number and its simulation's number using the algorithm
     * described in [UJJ3/166]. This implementation of hash code computation is
     * fully consistent with equals().
     *
     * @return A hash code for this process.
     */
    public int hashCode() {
        int temp = 17; // Magic number 17
        int myNumberAsInt = (int) (myNumber ^ (myNumber >>> 32));
        int simulationNumber = myParent.getSimulationNumber();

        temp = 37 * temp + myNumberAsInt; // Another magic number 37
        temp = 37 * temp + simulationNumber;

        return temp;
    } // hashCode
} // class JSimProcess
