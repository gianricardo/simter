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
import simulador.rotas.FilaNaviosEntradaToPraticoRt;

/**
 *
 * @author Eduardo
 */
public class FilaNavios extends JSimHead {   
    
    private FilaNaviosEntradaToPraticoRt _rotaEntradaPratico;
    
    public FilaNavios(String name, JSimSimulation sim)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException
    {
        super(name, sim);
    } // constructor

    public FilaNaviosEntradaToPraticoRt getRotaEntradaPratico() {
        return _rotaEntradaPratico;
    }    

    public void setRotaEntradaPratico(FilaNaviosEntradaToPraticoRt _rotaEntradaPratico) {
        this._rotaEntradaPratico = _rotaEntradaPratico;
    }
}
