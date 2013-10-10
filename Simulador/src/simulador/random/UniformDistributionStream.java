/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.random;

import cz.zcu.fav.kiv.jsim.random.JSimUniformStream;

/**
 *
 * @author gian
 */
public class UniformDistributionStream implements DistributionFunctionStream{

    private JSimUniformStream _aleatorio;

    public UniformDistributionStream(JSimUniformStream _uniform) {
        this._aleatorio = _uniform;
    }
        
    @Override
    public double getNext() {
        return _aleatorio.getNext();
    }
    
}
