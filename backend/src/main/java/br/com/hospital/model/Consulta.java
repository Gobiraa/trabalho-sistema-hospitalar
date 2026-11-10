package br.com.hospital.model;

import java.time.LocalDateTime;

import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.StatusConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Comportamento comum às consultas, incluindo a máquina de estados da situação. */
@Entity
@Table(name = "consultas")
public abstract class Consulta extends Atendimento {

    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    /** Validação da regra que cada tipo de consulta exige do profissional. */
    protected abstract void validarProfissional();

    @Override
    public void validarRealizacao() {
        if (getPaciente() == null) throw new RegraNegocioException("A consulta deve estar associada a um paciente.");
        if (getProfissional() == null) throw new RegraNegocioException("A consulta deve ter um profissional responsável.");
        if (getData() == null || getHorario() == null) throw new RegraNegocioException("Data e horário são obrigatórios.");
        if (motivo == null || motivo.isBlank()) throw new RegraNegocioException("O motivo da consulta é obrigatório.");
        validarProfissional();
    }

    /** Impede agendar no passado. */
    public void validarAgendamentoFuturo() {
        if (LocalDateTime.of(getData(), getHorario()).isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar uma consulta no passado.");
        }
    }

    @Override
    public boolean ocupaAgenda() { return status.ocupaAgenda(); }

    public void confirmar() { transicionar(StatusConsulta.CONFIRMADA); }
    public void iniciarAtendimento() { transicionar(StatusConsulta.EM_ATENDIMENTO); }
    public void cancelar() { transicionar(StatusConsulta.CANCELADA); }
    public void registrarNaoComparecimento() { transicionar(StatusConsulta.NAO_COMPARECEU); }

    public void concluir(String observacoesMedicas) {
        transicionar(StatusConsulta.CONCLUIDA);
        if (observacoesMedicas != null && !observacoesMedicas.isBlank()) setObservacoes(observacoesMedicas);
    }

    /** Observações só podem ser registradas durante ou após o atendimento. */
    public void registrarObservacoes(String observacoes) {
        if (status != StatusConsulta.EM_ATENDIMENTO && status != StatusConsulta.CONCLUIDA) {
            throw new RegraNegocioException("Observações só podem ser registradas durante ou após o atendimento (situação atual: " + status + ").");
        }
        setObservacoes(observacoes);
    }

    /** Reagendamento só é permitido enquanto a consulta ainda não começou. */
    public void validarPodeSerAlterada() {
        if (status != StatusConsulta.AGENDADA && status != StatusConsulta.CONFIRMADA) {
            throw new RegraNegocioException("Consulta na situação " + status + " não pode ser alterada.");
        }
    }

    private void transicionar(StatusConsulta destino) {
        if (!status.podeIrPara(destino)) {
            throw new RegraNegocioException("Transição inválida de consulta: " + status + " -> " + destino
                    + ". Para uma nova consulta é necessário um novo agendamento.");
        }
        this.status = destino;
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public StatusConsulta getStatus() { return status; }
}
