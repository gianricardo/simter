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
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoEstacao;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class DecisaoPosicaoToEstacaoArmazenamentoIntRt extends RouteBase {

    private List<CaminhaoPatio> _caminhoes = new ArrayList(); 
        
    private DecisaoCaminhaoPatioPosicaoEstacao _decisaoToPosicoesEstacao;
    private PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno _posicaoEstacaoInterna;

    public DecisaoPosicaoToEstacaoArmazenamentoIntRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
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
                            ElementOut();                                                            
                        }
                        else{
                            passivate();
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
            _caminhoes.get(0).escreverArquivo(" -Colocado na " + getName() + " no momento " + myParent.getCurrentTime());
            super.OcuparRota();
            return true;
        }
    }

    public void ElementOut() {
        try {
            while (true) {
                if (_posicaoEstacaoInterna.getCaminhao()==null) {
                    _posicaoEstacaoInterna.setCaminhao(_caminhoes.get(0));
                    super.LiberarRota();
                    _caminhoes.remove(0);
                    if (_posicaoEstacaoInterna.isIdle()) {
                        _posicaoEstacaoInterna.activate(myParent.getCurrentTime());
                    }
                    if (_decisaoToPosicoesEstacao.isIdle()) {
                        _decisaoToPosicoesEstacao.activate(myParent.getCurrentTime());
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

    public void setDecisaoToPosicoesEstacao(DecisaoCaminhaoPatioPosicaoEstacao _decisaoToPosicoesEstacao) {
        this._decisaoToPosicoesEstacao = _decisaoToPosicoesEstacao;
    }

    public void setPosicaoEstacaoInterna(PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno _posicaoEstacaoInterna) {
        this._posicaoEstacaoInterna = _posicaoEstacaoInterna;
    }
}
