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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private List<SolicitacaoCaminhoesPatio> _listaSolicitacoesCaminhoes = new ArrayList();
    private List<DecisaoPosicaoToEstacaoArmazenamentoIntRt> _listaRotasPosicoesEstacaoDestino = new ArrayList();
    private List<PosicaoBercoToDecisaoPosicaoEstacaoRt> _listaRotasPosicaoBercoOrigem = new ArrayList();
    private DecisaoEstacaoToDecisaoBercoRt _rotaDecisaoBerco;
    private FilaCaminhoesInternos _filaCaminhoesVazios;
    private CaminhaoPatio _caminhao;
    private int indiceUltimaSolicitacaoAtendida = 0;
    private int indiceUltimaRotaVerificada = 0;
    private SolicitacaoCaminhoesPatio _solicitacaoMomento;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public DecisaoCaminhaoPatioPosicaoEstacao(String idDecisao, JSimSimulation simulation, FilaCaminhoesInternos filaCaminhoesVazios, DecisaoEstacaoToDecisaoBercoRt rotaDecisaoBerco)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idDecisao, simulation);
        _filaCaminhoesVazios = filaCaminhoesVazios;
        _rotaDecisaoBerco = rotaDecisaoBerco;

        criarArquivo();
    }

    @Override
    protected void life() {
        while (true) {
            if (_listaSolicitacoesCaminhoes.isEmpty() && _caminhao == null) {
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
                                escreverArquivo("\r\nAdicionando caminhão " + _caminhao.getIdCaminhao() + " da fila de caminhões vazios" + " no momento " + myParent.getCurrentTime());
                                _caminhao.escreverArquivo("\r\n -Entrou na " + this.getName() + " no momento " + myParent.getCurrentTime());
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
                    } else {
                        verificarProximaSolicitacao();
                    }
                } else {
                    if (!_caminhao.isMovimentacaoFinalizada()) {
                        try {
                            passivate();
                        } catch (JSimSecurityException ex) {
                            Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        verificarProximaSolicitacao();
                    }
                }
            }
        }
    }

    public void verificarCaminhoesRotas() {
        for (int i = 0/*indiceUltimaRotaVerificada*/; i < _listaRotasPosicaoBercoOrigem.size(); i++) {
            _caminhao = _listaRotasPosicaoBercoOrigem.get(i).VerificarCaminhaoFinalizado();
            if (_caminhao != null) {
                _caminhao.escreverArquivo("\r\n -Entrou na " + getName() + " no momento " + myParent.getCurrentTime());
                escreverArquivo("\r\nAdicionando caminhão " + _caminhao.getIdCaminhao() + " da rota " + _listaRotasPosicaoBercoOrigem.get(i).getName() + " no momento " + myParent.getCurrentTime());
                _caminhao.setMovimentacaoFinalizada(false);
                indiceUltimaRotaVerificada++;
                if (indiceUltimaRotaVerificada >= _listaRotasPosicaoBercoOrigem.size()) {
                    indiceUltimaRotaVerificada = 0;
                }
                //_caminhao.setContainer(null);
                if (_listaRotasPosicaoBercoOrigem.get(i).isIdle()) {
                    try {
                        _listaRotasPosicaoBercoOrigem.get(i).activate(myParent.getCurrentTime());
                    } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                        Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }
        }
    }

    public void verificarProximaSolicitacao() {

        if (!_listaSolicitacoesCaminhoes.isEmpty()) {
            _solicitacaoMomento = _listaSolicitacoesCaminhoes.get(indiceUltimaSolicitacaoAtendida);


            if (_solicitacaoMomento.getNumeroCaminhoesDescarregar() > 0) {
                _caminhao.setRotaPosicaoBercoAposDecisao(_solicitacaoMomento.getRotaDecisaoPosicaoBerco());
                _solicitacaoMomento.setNumeroCaminhoesDescarregar(_solicitacaoMomento.getNumeroCaminhoesDescarregar() - 1);
                if (_solicitacaoMomento.getNumeroCaminhoesDescarregar() <= 0) {
                    _listaSolicitacoesCaminhoes.remove(_solicitacaoMomento);
                }
                if (_caminhao.getContainer() == null) {
                    DecidirCaminhaoVazioDescarregarNavio();
                } else {
                    try {
                        DecidirCaminhaoOcupadoDescarregarNavio();
                    } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                        Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                indiceUltimaSolicitacaoAtendida++;
                if (indiceUltimaSolicitacaoAtendida >= _listaSolicitacoesCaminhoes.size()) {
                    indiceUltimaSolicitacaoAtendida = 0;
                }
            } else if (_solicitacaoMomento.getNumeroCaminhoesCarregar() > 0) {
                _caminhao.setRotaPosicaoBercoAposDecisao(_solicitacaoMomento.getRotaDecisaoPosicaoBerco());
                _solicitacaoMomento.setNumeroCaminhoesCarregar(_solicitacaoMomento.getNumeroCaminhoesCarregar() - 1);
                if (_caminhao.getContainer() == null) {
                    DecidirCaminhaoVazioCarregarNavio();
                } else {
                    DecidirCaminhaoOcupadoCarregarNavio();
                }
            }
        } else {
            CaminhaoSemOperacao();
        }
    }

    public void DecidirCaminhaoVazioDescarregarNavio() {
        _caminhao.setOperacao(CaminhaoOperacao.Carregar);
        while (true) {
            if (!_rotaDecisaoBerco.GetNextElement(_caminhao)) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                escreverArquivo("Enviando caminhão " + _caminhao.getIdCaminhao() + " para rota " + _rotaDecisaoBerco.getName() + " no momento " + myParent.getCurrentTime());
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

    public void DecidirCaminhaoOcupadoDescarregarNavio() throws JSimSecurityException, JSimInvalidParametersException {
        DecisaoPosicaoToEstacaoArmazenamentoIntRt _rotaMomento;
        _caminhao.setOperacao(CaminhaoOperacao.Descarregar);
        while (true) {
            _rotaMomento = ElementOut();
            if (_rotaMomento == null) {
                passivate();
            } else {
                if (_rotaMomento.isIdle()) {
                    _rotaMomento.activate(myParent.getCurrentTime());
                }
                break;
            }
        }
    }

    public void DecidirCaminhaoVazioCarregarNavio() {
    }

    public void DecidirCaminhaoOcupadoCarregarNavio() {
    }

    public void CaminhaoSemOperacao() {
        _caminhao.setRotaPosicaoBercoAposDecisao(null);
        if (_caminhao.getContainer() == null) {
            try {
                _caminhao.into(_filaCaminhoesVazios);
                _caminhao.escreverArquivo("\r\n -Entrou na Fila de Caminhões Vazios no momento " + myParent.getCurrentTime() + " no momento " + myParent.getCurrentTime());
                _caminhao = null;
            } catch (JSimSecurityException ex) {
                Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            DecidirCaminhaoOcupadoSemOperacaoNavio();
        }
    }

    public void DecidirCaminhaoOcupadoSemOperacaoNavio() {
        DecisaoPosicaoToEstacaoArmazenamentoIntRt _rotaMomento;
        _caminhao.setOperacao(CaminhaoOperacao.Descarregar);
        while (true) {
            _rotaMomento = ElementOut();
            if (_rotaMomento == null) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (_rotaMomento.isIdle()) {
                    try {
                        _rotaMomento.activate(myParent.getCurrentTime());
                    } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                        Logger.getLogger(DecisaoCaminhaoPatioPosicaoEstacao.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }
        }
    }

    public DecisaoPosicaoToEstacaoArmazenamentoIntRt ElementOut() {
        for (int i = 0; i < _listaRotasPosicoesEstacaoDestino.size(); i++) {
            if (_listaRotasPosicoesEstacaoDestino.get(i).GetNextElement(_caminhao)) {
                escreverArquivo("Enviando caminhão " + _caminhao.getIdCaminhao() + " para rota " + _listaRotasPosicoesEstacaoDestino.get(i).getName() + " no momento " + myParent.getCurrentTime());
                _caminhao = null;
                return _listaRotasPosicoesEstacaoDestino.get(i);
            }
        }
        return null;
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

    public void AdicionarSolicitacao(SolicitacaoCaminhoesPatio solicitacao) {
        _listaSolicitacoesCaminhoes.add(solicitacao);
    }

    public void addListaRotasPosicoesEstacaoDestino(DecisaoPosicaoToEstacaoArmazenamentoIntRt _listaRotasPosicoesEstacaoDestino) {
        this._listaRotasPosicoesEstacaoDestino.add(_listaRotasPosicoesEstacaoDestino);
    }

    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivo" + getName() + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo(String texto) {
        try {
            _bw.write("\r\n " + texto);
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
