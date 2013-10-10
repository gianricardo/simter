/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.random;

import cz.zcu.fav.kiv.jsim.random.JSimExponentialStream;

/**
 *
 * @author Eduardo
 */
public class ExponentialDistributionStream implements DistributionFunctionStream{

    private JSimExponentialStream _aleatorio;

    public ExponentialDistributionStream(JSimExponentialStream _exponential) {
        this._aleatorio = _exponential;
    }
        
    @Override
    public double getNext() {
        return _aleatorio.getNext();
    }
    
}
