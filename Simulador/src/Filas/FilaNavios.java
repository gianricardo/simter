/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Filas;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class FilaNavios extends JSimHead {
    
    private JSimProcess berco;
    
    public FilaNavios(String name, JSimSimulation sim, JSimProcess b)
            throws JSimInvalidParametersException, JSimTooManyHeadsException
    {
        super(name, sim);
	berco = b;
    } // constructor
    
    public JSimProcess getBerco()
    {
            return berco;
    } // getServer

    public void setBerco(JSimProcess b)
    {
            berco = b;
    } // setServer
    
    public int IdBerco;
    public List listaNavios = new ArrayList();        
}
