/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.queues;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class FilaCaminhoesExternos extends JSimHead {    
    
    public FilaCaminhoesExternos(String name, JSimSimulation sim)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException
    {
        super(name, sim);	
    } // constructor
    
}
