/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.sea;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import simulador.rotas.FilaNaviosEntradaToPraticoRt;
import simulador.rotas.PraticoToBercoRt;

/**
 *
 * @author Eduardo
 */
public class Pratico extends JSimProcess {

    private List<PraticoToBercoRt> _listaRotasBerco = new ArrayList();
    FilaNaviosEntradaToPraticoRt _rotaNavioPratico;
    private Navio _navio;
    private boolean _ocupado = false;

    public Pratico(String idPratico, JSimSimulation simulation)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idPratico, simulation);
    }

    @Override
    protected void life() {
        while (true) {
            
            if (_navio == null) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(Pratico.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                _ocupado = true;
                for (int i = 0; i < _listaRotasBerco.size(); i++) {
                    PraticoToBercoRt rota = _listaRotasBerco.get(i);
                    if (!rota.getBerco().isOcupado() && !rota.isOcupada()) {
                        try {
                            if (rota.GetNextElement(_navio)) {                               
                                _navio = null;
                                if (rota.isIdle()) {
                                    try {
                                        rota.activate(myParent.getCurrentTime());
                                    } catch (JSimInvalidParametersException ex) {
                                        Logger.getLogger(Pratico.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        } catch (JSimSecurityException ex) {
                            Logger.getLogger(Pratico.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    } 
                }
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(Pratico.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void setNavio(Navio _navio) {
        this._navio = _navio;
    }

    public boolean isOcupado() {
        return _ocupado;
    }

    public void addListaRotasBerco(PraticoToBercoRt RotaBerco) {
        this._listaRotasBerco.add(RotaBerco);
    }

    public void setOcupado(boolean _ocupado) {
        this._ocupado = _ocupado;
        if (_ocupado == false && _rotaNavioPratico.isIdle()) {
            try {
                _rotaNavioPratico.activate(myParent.getCurrentTime());
            } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                Logger.getLogger(Pratico.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public FilaNaviosEntradaToPraticoRt getRotaNavioPratico() {
        return _rotaNavioPratico;
    }

    public void setRotaNavioPratico(FilaNaviosEntradaToPraticoRt rotaNavioPratico) {
        this._rotaNavioPratico = rotaNavioPratico;
    }

    public void getNextElement() {
        _rotaNavioPratico.ElementOut();
    }
}
