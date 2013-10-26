/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package negocio;

import Enumerators.CaminhaoOperacao;
import Enumerators.ContainerTipos;
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
            if (_transtainer.getPosicaoCargaDescarga().getCaminhao() == null && _transtainer.getPosicaoCargaDescargaInterna().getCaminhao() == null) {
                _transtainer.passivo();
            } else {
                if (_transtainer.getPosicaoCargaDescarga().getCaminhao() != null) {
                    atenderPosicaoExterna();
                } else if (_transtainer.getPosicaoCargaDescargaInterna().getCaminhao() != null) {
                    atenderPosicaoInterna();
                }
                _transtainer.passivo();
            }
        } // while
    } // life   

    public void atenderPosicaoExterna() {
        if (caminhaoOcupado(_transtainer.getPosicaoCargaDescarga().getCaminhao())) {
            descarregarCaminhao();
            if (caminhaoFinalizado()) {
                if (_transtainer.getPosicaoCargaDescarga().isIdle()) {
                    try {
                        _transtainer.getPosicaoCargaDescarga().activate(_transtainer.getSimulation().getCurrentTime());
                    } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
        }
    }

    public void atenderPosicaoInterna() {
        if (caminhaoOcupado(_transtainer.getPosicaoCargaDescargaInterna().getCaminhao())) {
            descarregarCaminhao();
            if (caminhaoFinalizado()) {
                if (_transtainer.getPosicaoCargaDescargaInterna().isIdle()) {
                    try {
                        _transtainer.getPosicaoCargaDescargaInterna().activate(_transtainer.getSimulation().getCurrentTime());
                    } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
        }
    }

    public boolean caminhaoOcupado(JSimLink jsl) {
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
            if (_caminhaoPatio.getContainer() != null) {
                return true;
            }
        }
        return false;
    }

    public void descarregarCaminhao() {
        if (_caminhaoExterno != null && (_caminhaoExterno.getOperacao() == CaminhaoOperacao.Descarregar || _caminhaoExterno.getOperacao() == CaminhaoOperacao.DescarregarCarregar)) {
            try {
                _transtainer.segurar(JSimSystem.uniform(10, 10));
                if (_caminhaoExterno.getContainer().getDestinoContainer() == ContainerTipos.CaminhaoExterno) {
                    try {
                        _caminhaoExterno.getContainer().into(_transtainer.getEstacaoArmazenamento().getFilaContainersParaCaminhoesExternos());
                    } catch (JSimSecurityException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        _caminhaoExterno.getContainer().into(_transtainer.getEstacaoArmazenamento().getFilaContainersParaNavio());
                    } catch (JSimSecurityException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
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
            try {
                _transtainer.segurar(JSimSystem.uniform(10, 10));
                if (_caminhaoPatio.getContainer().getDestinoContainer() == ContainerTipos.CaminhaoExterno) {
                    try {
                        JSimLink _container = _caminhaoPatio.getContainer();
                        _container.into(_transtainer.getEstacaoArmazenamento().getFilaContainersParaCaminhoesExternos());
                    } catch (JSimSecurityException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        _caminhaoPatio.getContainer().into(_transtainer.getEstacaoArmazenamento().getFilaContainersParaNavio());
                    } catch (JSimSecurityException ex) {
                        Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                _caminhaoPatio.setContainer(null);
                _caminhaoPatio.setCarregado(false);
                _transtainer.getEstacaoArmazenamento().incrementQuantidadeCargaMomento();
                if (_caminhaoPatio.getOperacao() == CaminhaoOperacao.Descarregar) {
                    _caminhaoPatio.setFinalizado(true);
                }
            } catch (JSimInvalidParametersException ex) {
                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                container.out();
                _caminhaoPatio.setContainer((Container) container);
                _caminhaoPatio.setCarregado(true);
                _transtainer.getEstacaoArmazenamento().decrementQuantidadeCargaMomento();
                _caminhaoPatio.setFinalizado(true);
            } catch (JSimSecurityException ex) {
                Logger.getLogger(TranstainerBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean caminhaoFinalizado() {
        if (_caminhaoExterno != null && _caminhaoExterno.isFinalizado()) {
            return true;
        } else if (_caminhaoPatio != null && _caminhaoPatio.isFinalizado()) {
            return true;
        }
        return false;
    }
}
