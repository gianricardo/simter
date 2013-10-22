/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;

/**
 *
 * @author Eduardo
 */
public class SolicitacaoCaminhoesPatio {
    
    private int _numeroCaminhoesCarregar;
    private int _numeroCaminhoesDescarregar;
    
    private DecisaoPosicaoToPosicaoBercoRt _rotaDecisaoPosicaoBerco;

    public int getNumeroCaminhoesCarregar() {
        return _numeroCaminhoesCarregar;
    }

    public void setNumeroCaminhoesCarregar(int _numeroCaminhoesCarregar) {
        this._numeroCaminhoesCarregar = _numeroCaminhoesCarregar;
    }

    public int getNumeroCaminhoesDescarregar() {
        return _numeroCaminhoesDescarregar;
    }

    public void setNumeroCaminhoesDescarregar(int _numeroCaminhoesDescarregar) {
        this._numeroCaminhoesDescarregar = _numeroCaminhoesDescarregar;
    }   

    public DecisaoPosicaoToPosicaoBercoRt getRotaDecisaoPosicaoBerco() {
        return _rotaDecisaoPosicaoBerco;
    }

    public void setRotaDecisaoPosicaoBerco(DecisaoPosicaoToPosicaoBercoRt _rotaDecisaoPosicaoBerco) {
        this._rotaDecisaoPosicaoBerco = _rotaDecisaoPosicaoBerco;
    }    
}
