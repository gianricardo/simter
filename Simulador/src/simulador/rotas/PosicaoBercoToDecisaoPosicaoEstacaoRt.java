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
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class PosicaoBercoToDecisaoPosicaoEstacaoRt extends RouteBase {

    private List<CaminhaoPatio> _caminhoes = new ArrayList();
    private DecisaoCaminhaoPatioPosicaoEstacao _decisaoPosicaoEstacao;
    private PosicaoCargaDescargaBerco _posicaoBerco;

    public PosicaoBercoToDecisaoPosicaoEstacaoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
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
                    _caminhoes.get(0).setMovimentacaoFinalizada(false);
                    _caminhoes.get(0).setFinalizado(false);
                    hold(super.getStream().getNext());
                    ElementOut();
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(DecisaoPosicaoToPosicaoBercoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void ElementOut() {
        try {
            if (_decisaoPosicaoEstacao.getCaminhao() == null) {
                _decisaoPosicaoEstacao.setCaminhao(_caminhoes.get(0));
                _caminhoes.get(0).setMovimentacaoFinalizada(true);
                super.LiberarRota();
                _decisaoPosicaoEstacao.escreverArquivo("\r\nAdicionando caminhão " + _caminhoes.get(0).getIdCaminhao() + " da rota " + this.getName()+ " no momento " + myParent.getCurrentTime());
                _caminhoes.get(0).escreverArquivo("\r\n -Entrou na " + _decisaoPosicaoEstacao.getName() + " no momento " + myParent.getCurrentTime());
                _caminhoes.remove(0);
                if (_decisaoPosicaoEstacao.isIdle()) {
                    _decisaoPosicaoEstacao.activate(myParent.getCurrentTime());
                }
                if (_posicaoBerco.isIdle()) {
                    _posicaoBerco.activate(myParent.getCurrentTime());
                }
            } else {
                if (!_caminhoes.isEmpty()) {
                    _caminhoes.get(0).setMovimentacaoFinalizada(true);
                }
                passivate();
            }
        } catch (JSimSecurityException | JSimInvalidParametersException ex) {
            Logger.getLogger(PraticoToBercoRt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public CaminhaoPatio VerificarCaminhaoFinalizado() {
        if (!_caminhoes.isEmpty() && _caminhoes.get(0).isMovimentacaoFinalizada()) {
            CaminhaoPatio caminhaoRetornado = _caminhoes.get(0);
            super.LiberarRota();
            _caminhoes.remove(0);
            if (_posicaoBerco.isIdle()) {
                try {
                    _posicaoBerco.activate(myParent.getCurrentTime());
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(PosicaoBercoToDecisaoPosicaoEstacaoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return caminhaoRetornado;
        } else {
            return null;
        }
    }

    public boolean AddCaminhao(CaminhaoPatio caminhao) {
        if (super.isOcupada()) {
            return false;
        } else {
            _caminhoes.add(caminhao);
            super.OcuparRota();
            _caminhoes.get(0).escreverArquivo(" -Colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());
            return true;
        }
    }

    public void setDecisaoPosicaoEstacao(DecisaoCaminhaoPatioPosicaoEstacao _decisaoPosicaoEstacao) {
        this._decisaoPosicaoEstacao = _decisaoPosicaoEstacao;
    }

    public void setPosicaoBerco(PosicaoCargaDescargaBerco _posicaoBerco) {
        this._posicaoBerco = _posicaoBerco;
    }
}
