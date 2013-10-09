/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class FilaCaminhoesInternos extends JSimHead {
   private JSimProcess posicaoCargaDescarga;
    
    public FilaCaminhoesInternos(String name, JSimSimulation sim, JSimProcess p)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException
    {
        super(name, sim);
	posicaoCargaDescarga = p;
    } // constructor
    
    public JSimProcess getPosicaoCargaDescarga()
    {
        return posicaoCargaDescarga;
    } // getServer

    public void setPosicaoCargaDescarga(JSimProcess p)
    {
        posicaoCargaDescarga = p;
    } // setServer  
}
