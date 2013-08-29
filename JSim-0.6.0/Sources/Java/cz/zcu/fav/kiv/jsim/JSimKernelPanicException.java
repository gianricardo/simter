/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

/**
 * The JSimKernelPanicException is thrown out when a serious error in J-Sim kernel occures. It should never be caught in order for the
 * program to terminate and inform the user.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.1
 */
public class JSimKernelPanicException extends RuntimeException
{
	/**
	 * Serialization identification.
	 */
	private static final long serialVersionUID = 1657081279738584689L;

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new JSimKernelPanicException.
	 */
	public JSimKernelPanicException()
	{
		super("An unexpected error occured inside J-Sim. J-Sim kernel in panic.\nPlease report to: `jaroslav@kacer.biz' and `jsim-support@mail.kiv.zcu.cz'.\nThank you.");
	} // constructor

	/**
	 * Creates a new JSimKernelPanicException. This version of constructor allows a cause of this exception to be stored now and retreived
	 * later.
	 */
	public JSimKernelPanicException(Throwable cause)
	{
		super("An unexpected error occured inside J-Sim. J-Sim kernel in panic.\nPlease report to: `jaroslav@kacer.biz' and `jsim-support@mail.kiv.zcu.cz'.\nThank you.", cause);
	} // constructor

} // class JSimKernelPanicException
