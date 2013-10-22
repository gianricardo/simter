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
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoEstacao;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class DecisaoEstacaoToDecisaoBercoRt extends RouteBase {

    private List<CaminhaoPatio> _caminhoes = new ArrayList(); 
    
    private DecisaoCaminhaoPatioPosicaoBerco _decisaoToPosicoesBerco;
    private DecisaoCaminhaoPatioPosicaoEstacao _decisaoToPosicoesEstacao;

    public DecisaoEstacaoToDecisaoBercoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream, 
                                            DecisaoCaminhaoPatioPosicaoBerco decisaoToPosicoesBerco)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);        
        _decisaoToPosicoesBerco = decisaoToPosicoesBerco;
    }

    @Override
    protected void life() {
        while (true) {
            if(_caminhoes.isEmpty()){
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoEstacaoToDecisaoBercoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                try {
                    hold(super.getStream().getNext());
                    while (true) {
                        if (!_caminhoes.isEmpty()) {
                            if (ElementOut()) {
                                if(_decisaoToPosicoesBerco.isIdle()){
                                    _decisaoToPosicoesBerco.activate(myParent.getCurrentTime());
                                }
                                if(_decisaoToPosicoesEstacao.isIdle()){
                                    _decisaoToPosicoesEstacao.activate(myParent.getCurrentTime());
                                }
                                break;
                            } else {
                                passivate();
                            }
                        }
                    }
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement(CaminhaoPatio elemento) {
        if (super.isOcupada()) {
            return false;
        } else {
            _caminhoes.add(elemento);
            _caminhoes.get(0).escreverArquivo(" colocado na " + getName() + " no momento " + myParent.getCurrentTime());
            super.OcuparRota();
            return true;
        }
    }

    public boolean ElementOut() {
        if(!_decisaoToPosicoesBerco.addCaminhao(_caminhoes.get(0))){
            return false;
        }
        else{
            super.LiberarRota();
            _caminhoes.remove(0);
            return true;
        }
    }

    public void setDecisaoToPosicoesEstacao(DecisaoCaminhaoPatioPosicaoEstacao _decisaoToPosicoesEstacao) {
        this._decisaoToPosicoesEstacao = _decisaoToPosicoesEstacao;
    }
}
