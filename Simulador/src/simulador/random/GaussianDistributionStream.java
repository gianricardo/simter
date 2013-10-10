/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.random;

import cz.zcu.fav.kiv.jsim.random.JSimGaussianStream;

/**
 *
 * @author Eduardo
 */
public class GaussianDistributionStream implements DistributionFunctionStream {
    
     private JSimGaussianStream _aleatorio;

    public GaussianDistributionStream(JSimGaussianStream _gaussian) {
        this._aleatorio = _gaussian;
    }
        
    @Override
    public double getNext() {
        return _aleatorio.getNext();
    }
}
