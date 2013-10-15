/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.load;

import Enumerators.ContainerTipos;
import cz.zcu.fav.kiv.jsim.JSimLink;

/**
 *
 * @author Eduardo
 */
public class Container extends JSimLink {
    
    private String _id;
    
    private float _tempoAtendimento;
    private double _timeOfCreation;
    private ContainerTipos _origemContainer;
    private ContainerTipos _destinoContainer;
    
    public Container(double time, String id, ContainerTipos origemContainer, ContainerTipos destinoContainer)
    {
            _timeOfCreation = time;
            _id = new StringBuilder()
                    .append(id)
                    .toString();
    } // constructor

    public String getId() {
        return _id;
    }

    public float getTempoAtendimento() {
        return _tempoAtendimento;
    }

    public double getTimeOfCreation() {
        return _timeOfCreation;
    }    

    public double getCreationTime()
    {
            return _timeOfCreation;
    } // getCreationTime
}
