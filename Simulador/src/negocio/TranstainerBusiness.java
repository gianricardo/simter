/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package negocio;

import Enumerators.CaminhaoOperacao;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoExterno;
import shipyard.land.move.CaminhaoPatio;
import shipyard.land.move.Transtainer;
import shipyard.load.Container;

/**
 *
 * @author Eduardo
 */
public class TranstainerBusiness {

    private Transtainer _transtainer;
    private CaminhaoExterno _caminhaoExterno;
    private CaminhaoPatio _caminhaoPatio;

    public TranstainerBusiness(Transtainer transtainer) {

        this._transtainer = transtainer;

    }

    public void life() {
        while (true) {
            if (_transtainer.getPosicaoCargaDescarga().getCaminhao() == null) {
                _transtainer.passivo();
            } else {
                if (caminhaoOcupado()) {
                    descarregarCaminhao();
                    if (caminhaoFinalizado()) {
                        if (_transtainer.getPosicaoCargaDescarga().isIdle()) {
                            try {
                                _transtainer.getPosicaoCargaDescarga().activate(_transtainer.getSimulation().getCurrentTime());
                            } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        _transtainer.passivo();
                    }
                } else {
                    carregarCaminhao();
                    if (_transtainer.getPosicaoCargaDescarga().isIdle()) {
                        try {
                            _transtainer.getPosicaoCargaDescarga().activate(_transtainer.getSimulation().getCurrentTime());
                        } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                            Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    _transtainer.passivo();
                }
            }
        } // while
    } // life    

    public boolean caminhaoOcupado() {
        JSimLink jsl = this._transtainer.getPosicaoCargaDescarga().getCaminhao();
        if (jsl instanceof CaminhaoExterno) {
            _caminhaoExterno = (CaminhaoExterno) jsl;
            _caminhaoPatio = null;
            if (_caminhaoExterno == null) {
                return false;
            }
            if (_caminhaoExterno.isCarregado()) {
                return true;
            }
        } else if (jsl instanceof CaminhaoPatio) {
            _caminhaoPatio = (CaminhaoPatio) jsl;
            _caminhaoExterno = null;
            if (_caminhaoPatio == null) {
                return false;
            }
            if (_caminhaoPatio.isCarregado()) {
                return true;
            }
        }
        return false;
    }

    public void descarregarCaminhao() {
        if (_caminhaoExterno != null && (_caminhaoExterno.getOperacao() == CaminhaoOperacao.Descarregar || _caminhaoExterno.getOperacao() == CaminhaoOperacao.DescarregarCarregar)) {
            try {
                _transtainer.segurar(JSimSystem.uniform(100, 100));
                _caminhaoExterno.escreverArquivo("\r\nDescarregou " + _caminhaoExterno.getContainer().getId());
                _caminhaoExterno.setContainer(null);
                _caminhaoExterno.setCarregado(false);
                _transtainer.getEstacaoArmazenamento().incrementQuantidadeCargaMomento();
                if (_caminhaoExterno.getOperacao() == CaminhaoOperacao.Descarregar) {
                    _caminhaoExterno.setFinalizado(true);
                }
            } catch (JSimInvalidParametersException ex) {
                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (_caminhaoPatio != null) {
            _caminhaoPatio.setContainer(null);
            _caminhaoPatio.setCarregado(false);
            _transtainer.getEstacaoArmazenamento().incrementQuantidadeCargaMomento();
        }
    }

    public void carregarCaminhao() {
        if (_caminhaoExterno != null && (_caminhaoExterno.getOperacao() == CaminhaoOperacao.Carregar || _caminhaoExterno.getOperacao() == CaminhaoOperacao.DescarregarCarregar)) {
            try {
                JSimLink container = _transtainer.getEstacaoArmazenamento().getFilaContainersParaCaminhoesExternos().first();
                container.out();
                _transtainer.segurar(JSimSystem.uniform(100, 100));
                _caminhaoExterno.setContainer((Container) container);
                _caminhaoExterno.setCarregado(true);                
                _transtainer.getEstacaoArmazenamento().decrementQuantidadeCargaMomento();
                _caminhaoExterno.setFinalizado(true);
            } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (_caminhaoPatio != null) {
            try {
                JSimLink container = _transtainer.getEstacaoArmazenamento().getFilaContainersParaNavio().first();
                _caminhaoPatio.setContainer((Container) container);
                _caminhaoPatio.setCarregado(true);
                container.out();
                _transtainer.getEstacaoArmazenamento().decrementQuantidadeCargaMomento();
            } catch (JSimSecurityException ex) {
                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean caminhaoFinalizado() {
        if (_caminhaoExterno != null && _caminhaoExterno.isFinalizado()) {
            return true;
        }
        return false;
    }
}
