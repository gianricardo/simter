/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
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
import shipyard.land.move.CaminhaoExterno;
import simulador.queues.FilaCaminhoesExternos;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class FilaCaminhoesExternosToDecisaoPosicaoRt extends RouteBase {

    private FilaCaminhoesExternos _filaCaminhoes;
    private List<CaminhaoExterno> _caminhoes = new ArrayList();
    private List<DecisaoPosicaoToEstacaoArmazenamentoRt> _rotasEstacaoArmazenamento = new ArrayList();
    private DecisaoPosicaoToEstacaoArmazenamentoRt _rotaMomento;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public FilaCaminhoesExternosToDecisaoPosicaoRt(String idRoute, JSimSimulation simulation, int capacidade, FilaCaminhoesExternos filaCaminhoes, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        _filaCaminhoes = filaCaminhoes;
        
        criarArquivo();
    }

    @Override
    protected void life() {
        while (true) {
            if (!GetNextElement()) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaCaminhoesExternosToDecisaoPosicaoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
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
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaCaminhoesExternosToDecisaoPosicaoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement() {
        if (super.isOcupada() || _filaCaminhoes.empty()) {            
            return false;
        } else {            
            JSimLink jsl = _filaCaminhoes.first();
            if (jsl instanceof CaminhaoExterno) {
                try {
                    CaminhaoExterno novoCaminhao = (CaminhaoExterno) jsl;
                    if (novoCaminhao == null) {
                        return false;
                    }
                    _caminhoes.add(novoCaminhao);
                    escreverArquivo("\r\n caminhao " + _caminhoes.get(0).getIdCaminhao() + " entrou no momento " + myParent.getCurrentTime());
                    _caminhoes.get(0).escreverArquivo("\r\nEntrou na " + this.getName() + " no momento " + myParent.getCurrentTime());
                    novoCaminhao.out();
                    super.OcuparRota();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaCaminhoesExternosToDecisaoPosicaoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println(jsl.getClass());
                return false;
            }
            return true;
        }
    }

    public DecisaoPosicaoToEstacaoArmazenamentoRt ElementOut() {
        for(int i = 0; i<_rotasEstacaoArmazenamento.size(); i++){
            if(_rotasEstacaoArmazenamento.get(i).addCaminhoes(_caminhoes.get(0))) {
                super.LiberarRota();
                escreverArquivo("\r\n caminhao " + _caminhoes.get(0).getIdCaminhao() + " saiu para rota " + _rotasEstacaoArmazenamento.get(i).getName() + " no momento " + myParent.getCurrentTime());
                _caminhoes.remove(0);                
                return _rotasEstacaoArmazenamento.get(i);
            }            
        }
        return null;
    }

    public void addRotasEstacaoArmazenamento(DecisaoPosicaoToEstacaoArmazenamentoRt _rotaEstacaoArmazenamento) {
        this._rotasEstacaoArmazenamento.add(_rotaEstacaoArmazenamento);
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivoRotaEntradaCaminhoes.txt");
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