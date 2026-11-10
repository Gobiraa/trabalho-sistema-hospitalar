package br.com.hospital.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos_clinicos_gerais")
public class MedicoClinicoGeral extends Medico {

    @Override
    public String getCategoria() { return "CLINICO_GERAL"; }

    @Override
    public String descreverAtribuicoes() {
        return "Consultas de rotina, avaliações iniciais e encaminhamento para atendimento especializado.";
    }
}
