/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package estatisticas;

/**
 *
 * @author Eduardo
 */
public class EstatisticasPorto {   
    
    private double _taxaOcupacaoPortainers;
    
    private double _taxaOcupacaoTranstainers;
    
    private double _tempoBercoOcupado;
    
    private int _qtdeContainersMovimentadaBerco;
    
    private int _qtdeContainersMovimentadaEstacao;
    
    private double _tempoTotalCaminhoesExternos;
    
    private int _qtdeCaminhoesExternosDeixaramPorto;
    
    private double _tempoTotalNavios;
    
    private int _qtdeNaviosDeixaramPorto;
    
    public void atualizarEstatisticasCaminhoes(double tempoAtendimento){
        _tempoTotalCaminhoesExternos += tempoAtendimento;
        _qtdeCaminhoesExternosDeixaramPorto++;
    }
    
    public double calcularTempoMedioCaminhoes(){
        return (_tempoTotalCaminhoesExternos/_qtdeCaminhoesExternosDeixaramPorto);
    }
    
    public void atualizarEstatisticasNavios(double tempoAtendimento){
        _tempoTotalNavios += tempoAtendimento;
        _qtdeNaviosDeixaramPorto++;
    }
    
    public double calcularTempoMedioNavios(){
        return (_tempoTotalNavios/_qtdeNaviosDeixaramPorto);
    }
    
    public void incrementarQtdeContainersMovimentadaBerco(){
        _qtdeContainersMovimentadaBerco++;
    }

    public int getQtdeContainersMovimentadaBerco() {
        return _qtdeContainersMovimentadaBerco;
    }
    
    public void incrementarQtdeContainersMovimentadaEstacao(){
        _qtdeContainersMovimentadaEstacao++;
    }

    public int getQtdeContainersMovimentadaEstacao() {
        return _qtdeContainersMovimentadaEstacao;
    }

    public int getQtdeCaminhoesExternosDeixaramPorto() {
        return _qtdeCaminhoesExternosDeixaramPorto;
    }

    public int getQtdeNaviosDeixaramPorto() {
        return _qtdeNaviosDeixaramPorto;
    }    
    
    public void atualizarTempoBercoOcupado(double tempoAtendimento){
        _tempoBercoOcupado += tempoAtendimento;
    }
    
    public double calcularTaxaOcupacaoBerco(double tempoTotalSimulacao){
        return (_tempoBercoOcupado/tempoTotalSimulacao);
    }
}
