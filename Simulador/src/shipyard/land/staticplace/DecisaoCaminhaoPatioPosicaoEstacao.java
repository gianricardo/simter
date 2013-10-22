/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import Enumerators.CaminhaoOperacao;
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
import simulador.queues.FilaCaminhoesInternos;
import simulador.rotas.DecisaoEstacaoToDecisaoBercoRt;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoIntRt;
import simulador.rotas.PosicaoBercoToDecisaoPosicaoEstacaoRt;
import utils.SolicitacaoCaminhoesPatio;

/**
 *
 * @author Eduardo
 */
public class DecisaoCaminhaoPatioPosicaoEstacao extends JSimProcess {

    private List<SolicitacaoCaminhoesPatio> listaSolicitacoesCaminhoes = new ArrayList();
    private List<DecisaoPosicaoToEstacaoArmazenamentoIntRt> _listaRotasPosicoesEstacaoDestino = new ArrayList();
    private List<PosicaoBercoToDecisaoPosicaoEstacaoRt> _listaRotasPosicaoBercoOrigem = new ArrayList();
    private DecisaoEstacaoToDecisaoBercoRt _rotaDecisaoBerco;
    private FilaCaminhoesInternos _filaCaminhoesVazios;
    private CaminhaoPatio _caminhao;
    private int indiceUltimaSolicitacaoAtendida = 0;
    private int indiceUltimaRotaVerificada = 0;
    private SolicitacaoCaminhoesPatio _solicitacaoMomento;

    public DecisaoCaminhaoPatioPosicaoEstacao(String idDecisao, JSimSimulation simulation, FilaCaminhoesInternos filaCaminhoesVazios, DecisaoEstacaoToDecisaoBercoRt rotaDecisaoBerco)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idDecisao, simulation);
        _filaCaminhoesVazios = filaCaminhoesVazios;
        _rotaDecisaoBerco = rotaDecisaoBerco;
    }

    @Override
    protected void life() {
        while (true) {
            if (listaSolicitacoesCaminhoes.isEmpty()) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (_caminhao == null) {
                    verificarCaminhoesRotas();
                    if (_caminhao == null) {
                        if (!_filaCaminhoesVazios.empty()) {
                            try {
                                _caminhao = (CaminhaoPatio) _filaCaminhoesVazios.first();
                                _caminhao.escreverArquivo("\r\nEntrou na " + this.getName() + " no momento " + myParent.getCurrentTime());
                                _caminhao.out();
                                verificarProximaSolicitacao();
                            } catch (JSimSecurityException ex) {
                                Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            try {
                                passivate();
                            } catch (JSimSecurityException ex) {
                                Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    else{
                        verificarProximaSolicitacao();
                    }
                } else {
                    verificarProximaSolicitacao();
                }
            }
        }
    }

    public void AdicionarSolicitacao(SolicitacaoCaminhoesPatio solicitacao) {
        listaSolicitacoesCaminhoes.add(solicitacao);
    }

    public void verificarCaminhoesRotas() {
        for (int i = indiceUltimaRotaVerificada; i < _listaRotasPosicaoBercoOrigem.size(); i++) {
            _caminhao = _listaRotasPosicaoBercoOrigem.get(i).VerificarCaminhaoFinalizado();            
            if (_caminhao != null) {
                _caminhao.escreverArquivo("\r\nEntrou na " + getName() + " no momento " + myParent.getCurrentTime());
                indiceUltimaRotaVerificada++;
                if(indiceUltimaRotaVerificada >= _listaRotasPosicaoBercoOrigem.size()){
                    indiceUltimaRotaVerificada = 0;
                }
                _caminhao.setContainer(null);
                break;
            }
        }
    }

    public void verificarProximaSolicitacao() {
        _solicitacaoMomento = listaSolicitacoesCaminhoes.get(indiceUltimaSolicitacaoAtendida);

        if (_solicitacaoMomento.getNumeroCaminhoesDescarregar() > 0) {
            _caminhao.setRotaPosicaoBercoAposDecisao(_solicitacaoMomento.getRotaDecisaoPosicaoBerco());
            if (_caminhao.getContainer() == null) {
                _caminhao.setOperacao(CaminhaoOperacao.Carregar);
                while (true) {
                    if (!_rotaDecisaoBerco.GetNextElement(_caminhao)) {
                        try {
                            passivate();
                        } catch (JSimSecurityException ex) {
                            Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        _caminhao = null;
                        if (_rotaDecisaoBerco.isIdle()) {
                            try {
                                _rotaDecisaoBerco.activate(myParent.getCurrentTime());
                            } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                                Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public CaminhaoPatio getCaminhao() {
        return _caminhao;
    }

    public void setCaminhao(CaminhaoPatio _caminhao) {
        this._caminhao = _caminhao;
    }

    public void addListaRotasPosicaoBercoOrigem(PosicaoBercoToDecisaoPosicaoEstacaoRt _listaRotasPosicaoBercoOrigem) {
        this._listaRotasPosicaoBercoOrigem.add(_listaRotasPosicaoBercoOrigem);
    }
}
