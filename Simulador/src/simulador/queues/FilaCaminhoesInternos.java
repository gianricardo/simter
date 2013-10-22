/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.queues;

import cz.zcu.fav.kiv.jsim.JSimHead;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoPatio;

/**
 *
 * @author Eduardo
 */
public class FilaCaminhoesInternos extends JSimHead {

    private JSimSimulation _simulation;
    private int _numeroCaminhoesCriacao;
    private CaminhaoPatio _caminhaoPatio;    

    public FilaCaminhoesInternos(String name, JSimSimulation sim, int numeroCaminhoesCriacao)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException {
        super(name, sim);
        _numeroCaminhoesCriacao = numeroCaminhoesCriacao;        
        IniciarFila();
    } // constructor
    
    public FilaCaminhoesInternos(String name, JSimSimulation sim)
            throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException {
        super(name, sim);
    } // constructor

    public void IniciarFila() {
        for (int i = 0; i < _numeroCaminhoesCriacao; i++) {
            try {
                _caminhaoPatio = new CaminhaoPatio(myParent.getCurrentTime(), String.valueOf(i), _simulation, 1);
                _caminhaoPatio.setCarregado(false);
                _caminhaoPatio.into(this);
                //escreverArquivo(_filaCaminhoesEstacao);
            } catch (JSimSecurityException ex) {
                Logger.getLogger(FilaCaminhoesInternos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
