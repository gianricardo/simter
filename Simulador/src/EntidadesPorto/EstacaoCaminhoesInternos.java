/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

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

/**
 *
 * @author Eduardo
 */
public class EstacaoCaminhoesInternos extends JSimProcess {
    private double lambda;
    public List CaminhoesEstacao = new ArrayList();
    private List FilasEstacaoPosicoesCargaDescarga = new ArrayList();
    private int numeroCaminhao = 1;
    private JSimSimulation simulation;
    private String NomeEstacao;

    public EstacaoCaminhoesInternos(String name, JSimSimulation sim, double l, int NumeroCaminhoes)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException, JSimSecurityException {
        super(name, sim);
        lambda = l;
        simulation = sim;
        NomeEstacao = name;
        
        for (int i = 0; i < NumeroCaminhoes; i++){
            CaminhaoPatio caminhao = new CaminhaoPatio(myParent.getCurrentTime(), String.valueOf(numeroCaminhao), super.getName(), simulation, 1);
            caminhao.carregado = false;
            CaminhoesEstacao.add(caminhao);
            numeroCaminhao++;
        }
    } // constructor    
    
    public FilaCaminhoesInternos SolicitarEstacao(PosicaoCargaDescargaBerco posicaoCargaDescarga) throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException, JSimSecurityException
    {
        FilaCaminhoesInternos filaCaminhoes = new FilaCaminhoesInternos("Fila de Caminhões " + this.NomeEstacao + " " + posicaoCargaDescarga.getName(), simulation, posicaoCargaDescarga);
        filaCaminhoes.setPosicaoCargaDescarga(posicaoCargaDescarga);
        FilasEstacaoPosicoesCargaDescarga.add(filaCaminhoes);
        VerificaCaminhoesVaziosInsereFila(filaCaminhoes);
        
        return filaCaminhoes;
    }
    
    public void VerificaCaminhoesVaziosInsereFila(FilaCaminhoesInternos fila) throws JSimSecurityException{
        for(int i = 0; i<CaminhoesEstacao.size(); i++)        
        {
            CaminhaoPatio caminhaoPatio = (CaminhaoPatio)CaminhoesEstacao.get(i);
            JSimLink NoCaminhao = new JSimLink(caminhaoPatio);
            NoCaminhao.into(fila);
        }
    }
}
