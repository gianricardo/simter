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
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class PosicaoEstacaoToDecisaoPosicaoBercoRt extends RouteBase{
    
    private PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno _posicaoInterna;
    private DecisaoCaminhaoPatioPosicaoBerco _decisaoCaminhoesBerco;
    
    private List<CaminhaoPatio> _caminhoes = new ArrayList(); 
    
    public PosicaoEstacaoToDecisaoPosicaoBercoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
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
                                if(_posicaoInterna.isIdle()){
                                    _posicaoInterna.activate(myParent.getCurrentTime());
                                }
                                if(_decisaoCaminhoesBerco.isIdle()){
                                    _decisaoCaminhoesBerco.activate(myParent.getCurrentTime());
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
            if (_caminhoes.get(0).getContainer() == null) {
                _caminhoes.get(0).escreverArquivo(" -Deixou a posição da Estação sem container.");
            } else {
                _caminhoes.get(0).escreverArquivo(" -Deixou a posição da Estação com o " + _caminhoes.get(0).getContainer().getId());
            }            
            _caminhoes.get(0).escreverArquivo(" -Colocado na " + getName() + " no momento " + myParent.getCurrentTime());
            super.OcuparRota();
            return true;
        }
    }

    public boolean ElementOut() {
        if(!_decisaoCaminhoesBerco.addCaminhao(_caminhoes.get(0))){
            return false;
        }
        else{
            super.LiberarRota();
            _caminhoes.remove(0);
            return true;
        }
    }

    public void setDecisaoCaminhoesBerco(DecisaoCaminhaoPatioPosicaoBerco _decisaoCaminhoesBerco) {
        this._decisaoCaminhoesBerco = _decisaoCaminhoesBerco;
    }

    public void setPosicaoInterna(PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno _posicaoInterna) {
        this._posicaoInterna = _posicaoInterna;
    }
}
