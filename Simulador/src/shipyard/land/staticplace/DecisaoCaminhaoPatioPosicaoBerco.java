/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

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
import shipyard.land.move.CaminhaoPatio;
import simulador.rotas.DecisaoEstacaoToDecisaoBercoRt;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoRt;
import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;
import simulador.rotas.PosicaoEstacaoToDecisaoPosicaoBercoRt;

/**
 *
 * @author Eduardo
 */
public class DecisaoCaminhaoPatioPosicaoBerco extends JSimProcess {
    
    private CaminhaoPatio _caminhao;    
    
    private List<PosicaoEstacaoToDecisaoPosicaoBercoRt> _listaRotasEstacao = new ArrayList();
    private DecisaoEstacaoToDecisaoBercoRt _rotaDecisaoEstacaoDecisaoBerco;    
    
    private DecisaoPosicaoToPosicaoBercoRt _rotaMomento;
    
    public DecisaoCaminhaoPatioPosicaoBerco(String id, JSimSimulation simulation)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(id, simulation);        
    }

    
    @Override
    protected void life() {
        while (true) {
            if (_caminhao == null) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoPosicaoToEstacaoArmazenamentoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    while (true) {
                        _rotaMomento = ElementOut();
                        if (_rotaMomento == null) {
                            passivate();
                        } else {
                            
                            if (_rotaMomento.isIdle()) {
                                _rotaMomento.activate(myParent.getCurrentTime());
                            }
                            
                            if(_rotaDecisaoEstacaoDecisaoBerco.isIdle()){
                                _rotaDecisaoEstacaoDecisaoBerco.activate(myParent.getCurrentTime());
                            }
                            
                            /*for(int i = 0; i<_listaRotasEstacao.size();i++)
                            {
                                if(_listaRotasEstacao.get(i).isIdle()){
                                    _listaRotasEstacao.get(i).activate(myParent.getCurrentTime());
                                }
                            }*/
                            break;
                        }
                    }
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(DecisaoPosicaoToEstacaoArmazenamentoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public DecisaoPosicaoToPosicaoBercoRt ElementOut() {
        DecisaoPosicaoToPosicaoBercoRt _rotaPosicaoBerco = _caminhao.getRotaPosicaoBercoAposDecisao();        
        if(!_rotaPosicaoBerco.GetNextElement(_caminhao)){
            return null;
        }
        else{
            _caminhao = null;
            return _rotaPosicaoBerco;
        }
    }

    public boolean addCaminhao(CaminhaoPatio caminhao) {
        if (_caminhao == null) {
            _caminhao = caminhao;
            _caminhao.escreverArquivo(" -Entrou na " + this.getName() + " no momento " + myParent.getCurrentTime());                        
            return true;
        }
        else{
            return false;            
        }        
    }

    public void addListaRotasEstacao(PosicaoEstacaoToDecisaoPosicaoBercoRt _listaRotasEstacao) {
        this._listaRotasEstacao.add(_listaRotasEstacao);
    }

    public void setRotaDecisaoEstacaoDecisaoBerco(DecisaoEstacaoToDecisaoBercoRt _rotaDecisaoEstacaoDecisaoBerco) {
        this._rotaDecisaoEstacaoDecisaoBerco = _rotaDecisaoEstacaoDecisaoBerco;
    }
}
