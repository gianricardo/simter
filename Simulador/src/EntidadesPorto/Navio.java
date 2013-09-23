/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimSystem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Navio {
    public String idNavio;    
    public float TempoAtendimento;
    public double timeOfCreation;
    public int NumeroContainersDescarregar = 0;
    public Date HoraChegadaPorto;
    public Date HoraSaidaPorto;
    public Date HoraAtracacao;
    public Date HoraSaidaBerco;
    public int NumeroRegioesNavio;
    
    List IdBercosPossiveis = new ArrayList();
    int idPratico;
    public float Calado;
    public float Comprimento;
    
    public Navio(double time, String id, String gerador, int numeroRegioesNavio)
    {
            timeOfCreation = time;
            idNavio = new StringBuilder()
                    .append(id)
                    .append(" ")
                    .append(gerador)
                    .toString();
            NumeroContainersDescarregar = (int) JSimSystem.uniform(500, 500);
            NumeroRegioesNavio = numeroRegioesNavio;
    } // constructor

    public double getCreationTime()
    {
            return timeOfCreation;
    } // getCreationTime
}
