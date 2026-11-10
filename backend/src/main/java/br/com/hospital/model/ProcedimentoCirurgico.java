package br.com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.StatusProcedimento;
import br.com.hospital.model.enums.TipoCirurgia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Data e horário previstos ficam em {@code data}/{@code horario} (herdados); os efetivos são próprios. */
@Entity
@Table(name = "procedimentos_cirurgicos")
public class ProcedimentoCirurgico extends Atendimento {

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCirurgia tipoCirurgia;

    @Column(length = 4000)
    private String observacoesPreOperatorias;
    @Column(length = 4000)
    private String observacoesPosOperatorias;

    private LocalDate dataEfetiva;
    private LocalTime horarioEfetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcedimento status = StatusProcedimento.AGENDADO;

    @Override
    public void validarRealizacao() {
        if (getPaciente() == null) throw new RegraNegocioException("O procedimento cirúrgico deve estar associado a um paciente.");
        if (!(getProfissional() instanceof Cirurgiao)) {
            throw new RegraNegocioException("O procedimento cirúrgico deve ter um cirurgião responsável.");
        }
        if (getData() == null || getHorario() == null) throw new RegraNegocioException("Data e horário previstos são obrigatórios.");
        if (descricao == null || descricao.isBlank()) throw new RegraNegocioException("A descrição do procedimento é obrigatória.");
        if (tipoCirurgia == null) throw new RegraNegocioException("O tipo de cirurgia é obrigatório.");
    }

    public void validarAgendamentoFuturo() {
        if (LocalDateTime.of(getData(), getHorario()).isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar um procedimento no passado.");
        }
    }

    @Override
    public boolean ocupaAgenda() { return status.ocupaAgenda(); }

    public void iniciar() {
        transicionar(StatusProcedimento.EM_ANDAMENTO);
        this.dataEfetiva = LocalDate.now();
        this.horarioEfetivo = LocalTime.now().withNano(0);
    }

    /** Só é possível concluir um procedimento que foi iniciado. */
    public void concluir(String observacoesPosOperatorias) {
        if (status != StatusProcedimento.EM_ANDAMENTO || dataEfetiva == null) {
            throw new RegraNegocioException("Não é possível concluir um procedimento que não foi iniciado (situação atual: " + status + ").");
        }
        transicionar(StatusProcedimento.CONCLUIDO);
        if (observacoesPosOperatorias != null && !observacoesPosOperatorias.isBlank()) {
            this.observacoesPosOperatorias = observacoesPosOperatorias;
        }
    }

    public void cancelar() { transicionar(StatusProcedimento.CANCELADO); }

    public void validarPodeSerAlterado() {
        if (status != StatusProcedimento.AGENDADO) {
            throw new RegraNegocioException("Procedimento na situação " + status + " não pode ser alterado.");
        }
    }

    private void transicionar(StatusProcedimento destino) {
        if (!status.podeIrPara(destino)) {
            throw new RegraNegocioException("Transição inválida de procedimento: " + status + " -> " + destino + ".");
        }
        this.status = destino;
    }

    @Override
    public String getModalidade() { return "PROCEDIMENTO_CIRURGICO"; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public TipoCirurgia getTipoCirurgia() { return tipoCirurgia; }
    public void setTipoCirurgia(TipoCirurgia tipoCirurgia) { this.tipoCirurgia = tipoCirurgia; }
    public String getObservacoesPreOperatorias() { return observacoesPreOperatorias; }
    public void setObservacoesPreOperatorias(String v) { this.observacoesPreOperatorias = v; }
    public String getObservacoesPosOperatorias() { return observacoesPosOperatorias; }
    public void setObservacoesPosOperatorias(String v) { this.observacoesPosOperatorias = v; }
    public LocalDate getDataEfetiva() { return dataEfetiva; }
    public LocalTime getHorarioEfetivo() { return horarioEfetivo; }
    public StatusProcedimento getStatus() { return status; }
}
