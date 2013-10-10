/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import shipyard.load.Container;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;

/**
 *
 * @author Eduardo
 */
public class CaminhaoPatio {   
    private int Capacidade;
    private String EstacaoCaminhoesInternos;
    public Container container;
    public double timeOfCreation;
    public String idCaminhao;
    public boolean carregado;
    public double HoraRecebimentoContainer;
    
     public CaminhaoPatio(double time, String id, String nomeEstacaoCaminhoesInternos, JSimSimulation simulation, int capacidade) throws JSimSecurityException
    {
            timeOfCreation = time;
            idCaminhao = new StringBuilder()
                    .append(id)
                    .append(" ")
                    .append(nomeEstacaoCaminhoesInternos)
                    .toString();            
            Capacidade = capacidade;
    } // constructor
}
