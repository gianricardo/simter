/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class RouteBase extends JSimProcess {
    private boolean _ocupada = false;
    private double _momentoOcupacao;
    private double _momentoLiberacao;
    private int _capacidade;
    private int _numeroElementosMomento = 0;
    private DistributionFunctionStream _stream;
    
    public RouteBase(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException{
        super(idRoute, simulation);
        this._capacidade = capacidade;
        _stream = stream;               
    }    
    
    public void OcuparRota(){
        _numeroElementosMomento ++;
        if(_capacidade == _numeroElementosMomento) {
            this._ocupada = true;
        }
    }
    
    public void LiberarRota(){
        _numeroElementosMomento --;
            this._ocupada = false;
    }

    public boolean isOcupada() {
        return _ocupada;
    }

    public DistributionFunctionStream getStream() {
        return _stream;
    }    
}
