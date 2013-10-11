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
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shipyard.land.move.CaminhaoPatio;
import simulador.queues.FilaCaminhoesInternos;

/**
 *
 * @author Eduardo
 */
public class EstacaoCaminhoesInternos extends JSimProcess {

    private List<CaminhaoPatio> _caminhoesEstacao = new ArrayList();
    private List<FilaCaminhoesInternos> _filasEstacaoPosicoesCargaDescarga = new ArrayList();
    private JSimSimulation _simulation;
    private String _nomeEstacao;
    private double _lambda;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    private CaminhaoPatio _caminhaoPatio;

    public EstacaoCaminhoesInternos(String name, JSimSimulation sim, double l, int NumeroCaminhoes)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException, JSimSecurityException {
        super(name, sim);
        _lambda = l;
        _simulation = sim;
        _nomeEstacao = name;

        for (int i = 0; i < NumeroCaminhoes; i++) {
            _caminhaoPatio = new CaminhaoPatio(myParent.getCurrentTime(), String.valueOf(i), super.getName(), _simulation, 1);
            _caminhaoPatio.setCarregado(false);
            _caminhoesEstacao.add(_caminhaoPatio);
        }

        criarArquivo();
    } // constructor    

    public List getFilasEstacaoPosicoesCargaDescarga() {
        return _filasEstacaoPosicoesCargaDescarga;
    }

    public List<CaminhaoPatio> getCaminhoesEstacao() {
        return _caminhoesEstacao;
    }

    public FilaCaminhoesInternos solicitarEstacao(PosicaoCargaDescargaBerco posicaoCargaDescarga, int NumeroCaminhoes)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException, JSimSecurityException {
        FilaCaminhoesInternos filaCaminhoes = new FilaCaminhoesInternos("Fila de Caminhões " + this._nomeEstacao + " " + posicaoCargaDescarga.getName(), _simulation, posicaoCargaDescarga);
        filaCaminhoes.setPosicaoCargaDescarga(posicaoCargaDescarga);
        _filasEstacaoPosicoesCargaDescarga.add(filaCaminhoes);
        verificaCaminhoesVaziosInsereFila(filaCaminhoes, NumeroCaminhoes);

        return filaCaminhoes;
    }

    private void verificaCaminhoesVaziosInsereFila(FilaCaminhoesInternos fila, int NumeroCaminhoesDescarregar)
            throws JSimSecurityException {
        for (int i = 0; i < NumeroCaminhoesDescarregar; i++) {
            if (!_caminhoesEstacao.isEmpty()) {
                _caminhaoPatio = _caminhoesEstacao.get(0);
                _caminhaoPatio.into(fila);
                _caminhoesEstacao.remove(_caminhaoPatio);
                escreverArquivo(fila);
            }
        }
    }

    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivoEstacaoCaminhoesInternos" + _nomeEstacao + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void escreverArquivo(FilaCaminhoesInternos fila) {
        try {
            _bw.write("\r\nEstação de caminhões Internos " + _nomeEstacao
                    + "\r\n -Caminhão " + _caminhaoPatio.getIdCaminhao()
                    + "\r\n -Colocado na fila " + fila.getHeadName()
                    + " \r\n");
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void closeBw() throws IOException {
        _bw.close();
    }
}
