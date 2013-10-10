/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.load;

import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Container extends JSimLink {
    
    private String _id;
    private float _medida;
    
    private float _tempoAtendimento;
    private double _timeOfCreation;
    
    public Container(double time, String id)
    {
            _timeOfCreation = time;
            _id = new StringBuilder()
                    .append(id)
                    .toString();
    } // constructor

    public String getId() {
        return _id;
    }

    public float getMedida() {
        return _medida;
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
