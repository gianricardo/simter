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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    
    public DecisaoPosicaoToPosicaoBercoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        
        criarArquivo();
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
            escreverArquivo("\r\n -Caminhao " + _caminhoes.get(0).getIdCaminhao() + " entrou na rota no momento " + myParent.getCurrentTime());
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
                    escreverArquivo(" -Caminhao " + _caminhoes.get(0).getIdCaminhao() + " saiu da rota no momento " + myParent.getCurrentTime());
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
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../Rotas/arquivo" + this.getName() + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo(String texto) {
        try {
            _bw.write("\r\n" + texto);
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
