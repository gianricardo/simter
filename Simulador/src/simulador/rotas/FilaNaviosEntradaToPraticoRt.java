/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
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
import shipyard.sea.Pratico;
import simulador.queues.FilaNavios;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class FilaNaviosEntradaToPraticoRt extends RouteBase {

    private FilaNavios _filaNavios;
    private Pratico _pratico;
    private List<Navio> _navios = new ArrayList();  
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public FilaNaviosEntradaToPraticoRt(String idRoute, JSimSimulation simulation, int capacidade, FilaNavios filaNavios, Pratico pratico, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        _filaNavios = filaNavios;
        _pratico = pratico;
        
        criarArquivo();
    }

    @Override
    protected void life() {        
        while (true) {
            if (!GetNextElement()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                    if(!_navios.isEmpty()){
                        ElementOut();
                    }
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    ElementOut();
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement() {
        if (super.isOcupada() || _filaNavios.empty()) { 
            return false;
        }
        else
        {
            JSimLink jsl = _filaNavios.first();
            if (jsl instanceof Navio) {
                try {
                    Navio novonavio = (Navio) jsl;
                    if(novonavio == null){
                        return false;
                    }                    
                    _navios.add(novonavio);
                    _navios.get(0).escreverArquivo(" -Colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());
                    escreverArquivo("\r\n -Navio " + _navios.get(0).getIdNavio() + " entrou na rota no momento " + myParent.getCurrentTime());
                    novonavio.out();                
                    super.OcuparRota();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println(jsl.getClass());
                return false;
            }
            return true;
        }
    }

    public void ElementOut() {
        try {
            if(!_pratico.isOcupado())
            {
                _pratico.setNavio(_navios.get(0));
                if(_pratico.isIdle()){                        
                    try {
                        _pratico.activate(myParent.getCurrentTime());
                    } catch (JSimInvalidParametersException ex) {
                        Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                super.LiberarRota();
                escreverArquivo(" -Navio " + _navios.get(0).getIdNavio() + " saiu da rota no momento " + myParent.getCurrentTime());
                _navios.remove(0);
            }
            else{
                passivate();
            }
        } catch (JSimSecurityException ex) {
            Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
        }
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
