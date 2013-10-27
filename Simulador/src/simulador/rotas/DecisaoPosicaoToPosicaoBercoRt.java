/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoPatio;
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoBerco;
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class DecisaoPosicaoToPosicaoBercoRt extends RouteBase {

    private List<CaminhaoPatio> _caminhoes = new ArrayList();
    private PosicaoCargaDescargaBerco _posicaoBerco;
    private DecisaoCaminhaoPatioPosicaoBerco _decisaoPosicoesBerco;

    public DecisaoPosicaoToPosicaoBercoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
    }

    @Override
    protected void life() {
        while (true) {
            if (_caminhoes.isEmpty()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    ElementOut();
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(DecisaoPosicaoToPosicaoBercoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement(CaminhaoPatio elemento) {
        if (super.isOcupada()) {
            return false;
        } else {
            _caminhoes.add(elemento);
            super.OcuparRota();
            _caminhoes.get(0).escreverArquivo(" -Colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());
            return true;
        }
    }

    public void ElementOut() {
        try {
            while (true) {
                if (!_posicaoBerco.isPosicaoOcupada()) {
                    _posicaoBerco.setCaminhao(_caminhoes.get(0));
                    _posicaoBerco.setPosicaoOcupada(true);
                    super.LiberarRota();
                    _caminhoes.remove(0);
                    if (_posicaoBerco.isIdle()) {
                        _posicaoBerco.activate(myParent.getCurrentTime());
                    }
                    if (_decisaoPosicoesBerco.isIdle()) {
                        _decisaoPosicoesBerco.activate(myParent.getCurrentTime());
                    }
                    break;
                }
                else{
                    passivate();
                }
            }
        } catch (JSimSecurityException | JSimInvalidParametersException ex) {
            Logger.getLogger(PraticoToBercoRt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPosicaoBerco(PosicaoCargaDescargaBerco _posicaoBerco) {
        this._posicaoBerco = _posicaoBerco;
    }

    public void setDecisaoPosicoesBerco(DecisaoCaminhaoPatioPosicaoBerco _decisaoPosicoesBerco) {
        this._decisaoPosicoesBerco = _decisaoPosicoesBerco;
    }
}
