/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Transportadores;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Navio {
    public float Calado;
    public float Comprimento;
    public Date HoraChegadaPorto;
    public Date HoraSaidaPorto;
    public Date HoraAtracacao;
    public Date HoraSaidaBerco;
    List IdBercosPossiveis = new ArrayList();
    int idPratico;
    public float TempoAtendimento;
    public double timeOfCreation;
    
    public Navio(double time)
    {
            timeOfCreation = time;
    } // constructor

    public double getCreationTime()
    {
            return timeOfCreation;
    } // getCreationTime
}
