/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.queues;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class FilaContainers extends JSimHead {

    private double _horaFinalAtendimento;
    private double _horaFinalDescarregamento;
    private double _horaFinalCarregamento;
    private double _horaInicioAtendimento;
    private String _nomeFila;
    private int _numeroContainers;
    private int _numeroContainersCarregar;    

    public FilaContainers(String name, JSimSimulation sim)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException {
        super(name, sim);        
        _nomeFila = name;
    } // constructor

    public double getHoraFinalAtendimento() {
        return _horaFinalAtendimento;
    }

    public void setHoraFinalAtendimento(double h) {
        _horaFinalAtendimento = h;
    }

    public double getHoraInicioAtendimento() {
        return _horaInicioAtendimento;
    }

    public void setHoraInicioAtendimento(double h) {
        _horaInicioAtendimento = h;
    }

    public int getNumeroContainers() {
        return _numeroContainers;
    }

    public void setNumeroContainers(int _numeroContainers) {
        this._numeroContainers = _numeroContainers;
    }    

    public String getNomeFila() {
        return _nomeFila;
    }

    public int getNumeroContainersCarregar() {
        return _numeroContainersCarregar;
    }

    public void setNumeroContainersCarregar(int _numeroContainersCarregar) {
        this._numeroContainersCarregar = _numeroContainersCarregar;
    }

    public void setHoraFinalDescarregamento(double _horaFinalDescarregamento) {
        this._horaFinalDescarregamento = _horaFinalDescarregamento;
}

    public void setHoraFinalCarregamento(double _horaFinalCarregamento) {
        this._horaFinalCarregamento = _horaFinalCarregamento;
    }
}
