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
    private JSimSimulation _simulation;
    private String _idNavio;    
    private float _tempoAtendimento;
    private double _timeOfCreation;
    private int _numeroContainersDescarregar = 0;
    private Date _horaChegadaPorto;
    private Date _horaSaidaPorto;
    private Date _horaAtracacao;
    private Date _horaSaidaBerco;
    private int _numeroRegioesNavio;
    private FilaContainers _filaContainer;
    private JSimLink _container;
    
    private List _filasContainers = new ArrayList();
    
    private List _idBercosPossiveis = new ArrayList();
    private int _idPratico;
    private float _calado;
    private float _comprimento;
    
    public Navio(double time, String id, String gerador, int numeroRegioesNavio, JSimSimulation simulation) throws JSimSecurityException
    {
            _timeOfCreation = time;
            _idNavio = id+" ";            
            _numeroContainersDescarregar = (int) JSimSystem.uniform(10, 10);
            
            _numeroRegioesNavio = numeroRegioesNavio;
            int aux = 1;
            int j;
            
            for(int i = 1; i <= _numeroRegioesNavio; i++){
                   try {
                       _filaContainer = new FilaContainers("Fila de Containers " + i + " navio " + _idNavio, simulation, null);
                    } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException ex) {
                        Logger.getLogger(EntidadesPorto.Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }                   
                   
                   for(j = aux; j<=i*(_numeroContainersDescarregar/numeroRegioesNavio); j++)
                   {
                       _container = new JSimLink(new Container(time, String.valueOf(j)));                       
                       _container.into(_filaContainer);
                   }
                   
                   if(i==_numeroRegioesNavio)
                   {
                       int auxiliarResto = _numeroContainersDescarregar - j;
                       for(int z = j; z<=j+auxiliarResto; z++)
                       {
                           _container = new JSimLink(new Container(time, String.valueOf(z)));                       
                           _container.into(_filaContainer);
                       }
                   }
                   aux = j;
                   
                   _filasContainers.add(_filaContainer);
            }
    } // constructor
    
    public double getCreationTime()
    {
            return _timeOfCreation;
    } // getCreationTime

    public JSimSimulation getSimulation() {
        return _simulation;
    }

    public String getIdNavio() {
        return _idNavio;
    }

    public float getTempoAtendimento() {
        return _tempoAtendimento;
    }

    public double getTimeOfCreation() {
        return _timeOfCreation;
    }

    public int getNumeroContainersDescarregar() {
        return _numeroContainersDescarregar;
    }

    public Date getHoraChegadaPorto() {
        return _horaChegadaPorto;
    }

    public Date getHoraSaidaPorto() {
        return _horaSaidaPorto;
    }

    public Date getHoraAtracacao() {
        return _horaAtracacao;
    }

    public Date getHoraSaidaBerco() {
        return _horaSaidaBerco;
    }

    public int getNumeroRegioesNavio() {
        return _numeroRegioesNavio;
    }

    public FilaContainers getFilaContainer() {
        return _filaContainer;
    }

    public JSimLink getContainer() {
        return _container;
    }

    public List getFilasContainers() {
        return _filasContainers;
    }

    public List getIdBercosPossiveis() {
        return _idBercosPossiveis;
    }

    public int getIdPratico() {
        return _idPratico;
    }

    public float getCalado() {
        return _calado;
    }

    public float getComprimento() {
        return _comprimento;
    }
    
    
    
}
