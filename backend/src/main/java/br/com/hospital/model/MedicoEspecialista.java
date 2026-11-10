package br.com.hospital.model;

import br.com.hospital.model.enums.Especialidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos_especialistas")
public class MedicoEspecialista extends Medico {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especialidade especialidade;

    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }

    @Override
    public String getCategoria() { return "ESPECIALISTA"; }

    @Override
    public String descreverAtribuicoes() {
        return "Atendimentos relacionados à especialidade " + especialidade + ".";
    }
}
