/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.sea.Navio;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class RotaSaidaNaviosRt extends RouteBase {
    
    private List<Navio> _navios = new ArrayList();
    private List<BercoToRotaSaidaRt> _rotasBercoToRotaSaida = new ArrayList();    
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public RotaSaidaNaviosRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        
        criarArquivo();
    }
    
    @Override
    protected void life() {
        while (true) {            
            if (_navios.isEmpty()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();                    
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    if(ElementOut()){
                        for(int i=0; i<_rotasBercoToRotaSaida.size(); i++){
                            if(_rotasBercoToRotaSaida.get(i).isIdle()){
                                _rotasBercoToRotaSaida.get(i).activate(myParent.getCurrentTime());
                            }
                        }
                    }
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public boolean GetNextElement(Navio elemento) {
        if (super.isOcupada()) {
            return false;
        } else {
            _navios.add(elemento);
            escreverArquivo("\r\n -Navio " + _navios.get(0).getIdNavio() + " entrou na rota no momento " + myParent.getCurrentTime());
            _navios.get(0).escreverArquivo(" -Colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());                
            return true;
        }
    }

    public boolean ElementOut() {
        _navios.get(0).escreverArquivo(" -Deixando o porto no momento " + myParent.getCurrentTime());
        escreverArquivo(" -Navio " + _navios.get(0).getIdNavio() + " saiu da rota no momento " + myParent.getCurrentTime());
        super.LiberarRota();
        _navios.remove(0);
        return true;
    }    
    
    public void AddRotaBercoToRotaSaida(BercoToRotaSaidaRt rota){
        _rotasBercoToRotaSaida.add(rota);
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../Rotas/arquivo" + this.getName() + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo(String texto) {
        try {
            _bw.write("\r\n" + texto);
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
