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
public class Container {
    
    public String idContainer;
    public float medida;
    
    public float tempoAtendimento;
    public double timeOfCreation;
    
    public Container(double time, String id)
    {
            timeOfCreation = time;
            idContainer = new StringBuilder()
                    .append(id)
                    .toString();
    } // constructor

    public double getCreationTime()
    {
            return timeOfCreation;
    } // getCreationTime
}
