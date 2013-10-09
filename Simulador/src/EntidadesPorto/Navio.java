/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class Navio {
    public JSimSimulation simulation;
    public String idNavio;    
    public float TempoAtendimento;
    public double timeOfCreation;
    public int NumeroContainersDescarregar = 0;
    public Date HoraChegadaPorto;
    public Date HoraSaidaPorto;
    public Date HoraAtracacao;
    public Date HoraSaidaBerco;
    public int NumeroRegioesNavio;
    public FilaContainers FilaContainer;
    public JSimLink container;
    
    public List FilasContainers = new ArrayList();
    
    List IdBercosPossiveis = new ArrayList();
    int idPratico;
    public float Calado;
    public float Comprimento;
    
    public Navio(double time, String id, String gerador, int numeroRegioesNavio, JSimSimulation simulation) throws JSimSecurityException
    {
            timeOfCreation = time;
            idNavio = new StringBuilder()
                    .append(id)
                    .append(" ")
                    .toString();
            
            NumeroContainersDescarregar = (int) JSimSystem.uniform(10, 10);
            
            NumeroRegioesNavio = numeroRegioesNavio;
            int aux = 1;
            int j;
            
            for(int i = 1; i <= NumeroRegioesNavio; i++){
                   try {
                       FilaContainer = new FilaContainers("Fila de Containers " + i + " navio " + idNavio, simulation, null);
                    } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException ex) {
                        Logger.getLogger(EntidadesPorto.Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }                   
                   
                   for(j = aux; j<=i*(NumeroContainersDescarregar/numeroRegioesNavio); j++)
                   {
                       container = new JSimLink(new Container(time, String.valueOf(j)));                       
                       container.into(FilaContainer);
                   }
                   
                   if(i==NumeroRegioesNavio)
                   {
                       int auxiliarResto = NumeroContainersDescarregar - j;
                       for(int z = j; z<=j+auxiliarResto; z++)
                       {
                           container = new JSimLink(new Container(time, String.valueOf(z)));                       
                           container.into(FilaContainer);
                       }
                   }
                   aux = j;
                   
                   FilasContainers.add(FilaContainer);
            }
    } // constructor
    
    public double getCreationTime()
    {
            return timeOfCreation;
    } // getCreationTime
}
