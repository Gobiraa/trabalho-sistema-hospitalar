package br.com.hospital.model;

import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.Especialidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "consultas_especializadas")
public class ConsultaEspecializada extends Consulta {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especialidade especialidade;

    @Override
    protected void validarProfissional() {
        if (especialidade == null) throw new RegraNegocioException("A especialidade da consulta é obrigatória.");
        if (!(getProfissional() instanceof MedicoEspecialista esp)) {
            throw new RegraNegocioException("Consulta especializada deve ser realizada por um médico especialista.");
        }
        if (esp.getEspecialidade() != especialidade) {
            throw new RegraNegocioException("A especialidade do profissional (" + esp.getEspecialidade()
                    + ") não é compatível com a consulta solicitada (" + especialidade + ").");
        }
    }

    @Override
    public String getModalidade() { return "ESPECIALIZADA"; }

    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
}
