/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import shipyard.load.Container;

/**
 *
 * @author Eduardo
 */
public class CaminhaoPatio extends JSimLink{   
    private int _capacidade;
    private String _estacaoCaminhoesInternos;
    private Container _container;
    private double _timeOfCreation;
    private String _idCaminhao;
    private boolean _carregado;
    private double _horaRecebimentoContainer;
    
     public CaminhaoPatio(double time, String id, String nomeEstacaoCaminhoesInternos, JSimSimulation simulation, int capacidade) throws JSimSecurityException
    {
            _timeOfCreation = time;
            _idCaminhao = new StringBuilder()
                    .append(id)
                    .append(" ")
                    .append(nomeEstacaoCaminhoesInternos)
                    .toString();            
            _capacidade = capacidade;
    } // constructor

    public int getCapacidade() {
        return _capacidade;
    }

    public String getEstacaoCaminhoesInternos() {
        return _estacaoCaminhoesInternos;
    }

    public Container getContainer() {
        return _container;
    }

    public double getTimeOfCreation() {
        return _timeOfCreation;
    }

    public String getIdCaminhao() {
        return _idCaminhao;
    }

    public boolean isCarregado() {
        return _carregado;
    }

    public double getHoraRecebimentoContainer() {
        return _horaRecebimentoContainer;
    }     

    public void setContainer(Container _container) {
        this._container = _container;
    }

    public void setCarregado(boolean _carregado) {
        this._carregado = _carregado;
    }

    public void setHoraRecebimentoContainer(double _horaRecebimentoContainer) {
        this._horaRecebimentoContainer = _horaRecebimentoContainer;
    }
}
