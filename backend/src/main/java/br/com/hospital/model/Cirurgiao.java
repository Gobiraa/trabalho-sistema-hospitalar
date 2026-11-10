package br.com.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cirurgioes")
public class Cirurgiao extends Medico {

    /** Área de atuação cirúrgica (ex.: cirurgia geral, cardiovascular). */
    @Column(nullable = false)
    private String areaCirurgica;

    public String getAreaCirurgica() { return areaCirurgica; }
    public void setAreaCirurgica(String areaCirurgica) { this.areaCirurgica = areaCirurgica; }

    @Override
    public String getCategoria() { return "CIRURGIAO"; }

    @Override
    public String descreverAtribuicoes() {
        return "Realização de procedimentos cirúrgicos (" + areaCirurgica + ").";
    }
}
