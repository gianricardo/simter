/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import simulador.queues.FilaCaminhoesInternos;
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shipyard.land.move.CaminhaoPatio;

/**
 *
 * @author Eduardo
 */
public class EstacaoCaminhoesInternos extends JSimProcess {
    private double _lambda;
    private List<CaminhaoPatio> _caminhoesEstacao = new ArrayList();
    private List<FilaCaminhoesInternos> _filasEstacaoPosicoesCargaDescarga = new ArrayList();
    private int _numeroCaminhao = 1;
    private JSimSimulation _simulation;
    private String _nomeEstacao;

    public EstacaoCaminhoesInternos(String name, JSimSimulation sim, double l, int NumeroCaminhoes)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException, JSimSecurityException {
        super(name, sim);
        _lambda = l;
        _simulation = sim;
        _nomeEstacao = name;
        
        for (int i = 0; i < NumeroCaminhoes; i++){
            CaminhaoPatio caminhao = new CaminhaoPatio(myParent.getCurrentTime(), String.valueOf(_numeroCaminhao), super.getName(), _simulation, 1);
            caminhao.carregado = false;
            _caminhoesEstacao.add(caminhao);
            _numeroCaminhao++;
        }
    } // constructor    

    public List getFilasEstacaoPosicoesCargaDescarga() {
        return _filasEstacaoPosicoesCargaDescarga;
    }

    public List<CaminhaoPatio> getCaminhoesEstacao() {
        return _caminhoesEstacao;
    }    
    
    public FilaCaminhoesInternos SolicitarEstacao(PosicaoCargaDescargaBerco posicaoCargaDescarga) throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException, JSimSecurityException
    {
        FilaCaminhoesInternos filaCaminhoes = new FilaCaminhoesInternos("Fila de Caminhões " + this._nomeEstacao + " " + posicaoCargaDescarga.getName(), _simulation, posicaoCargaDescarga);
        filaCaminhoes.setPosicaoCargaDescarga(posicaoCargaDescarga);
        _filasEstacaoPosicoesCargaDescarga.add(filaCaminhoes);
        VerificaCaminhoesVaziosInsereFila(filaCaminhoes);
        
        return filaCaminhoes;
    }
    
    public void VerificaCaminhoesVaziosInsereFila(FilaCaminhoesInternos fila) throws JSimSecurityException{
        for(int i = 0; i<_caminhoesEstacao.size(); i++)        
        {
            CaminhaoPatio caminhaoPatio = (CaminhaoPatio)_caminhoesEstacao.get(i);
            JSimLink NoCaminhao = new JSimLink(caminhaoPatio);
            NoCaminhao.into(fila);
        }
    }
}
