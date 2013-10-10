/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.queues;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class FilaContainers extends JSimHead {

    private JSimProcess portainer;
    private double horaFinalAtendimento;
    private double horaInicioAtendimento;
    public String nomeFila;

    public FilaContainers(String name, JSimSimulation sim, JSimProcess p)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException {
        super(name, sim);
        portainer = p;
        nomeFila = name;
    } // constructor

    public JSimProcess getPortainer() {
        return portainer;
    }

    public void setPortainer(JSimProcess p) {
        portainer = p;
    }

    public double getHoraFinalAtendimento() {
        return horaFinalAtendimento;
    }

    public void setHoraFinalAtendimento(double h) {
        horaFinalAtendimento = h;
    }

    public double getHoraInicioAtendimento() {
        return horaInicioAtendimento;
    }

    public void setHoraInicioAtendimento(double h) {
        horaInicioAtendimento = h;
    }
}
