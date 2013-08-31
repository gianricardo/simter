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
    public float Calado;
    public float Comprimento;
    public Date HoraChegadaPorto;
    public Date HoraSaidaPorto;
    public Date HoraAtracacao;
    public Date HoraSaidaBerco;
    List IdBercosPossiveis = new ArrayList();
    int idPratico;
    public float TempoAtendimento;
    public double timeOfCreation;
    public int NumeroContainersDescarregar = 0;
    
    public Navio(double time, String id, String gerador)
    {
            timeOfCreation = time;
            idNavio = new StringBuilder()
                    .append(id)
                    .append(" ")
                    .append(gerador)
                    .toString();
            NumeroContainersDescarregar = (int) JSimSystem.uniform(50, 50);
    } // constructor

    public double getCreationTime()
    {
            return timeOfCreation;
    } // getCreationTime
}
