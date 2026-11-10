package br.com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.PrioridadeEmergencia;
import br.com.hospital.model.enums.StatusEmergencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "atendimentos_emergencia")
public class AtendimentoEmergencia extends Atendimento {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeEmergencia prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmergencia status = StatusEmergencia.AGUARDANDO_AVALIACAO;

    private String queixa;

    /** Registra a chegada do paciente no momento atual. */
    public void registrarChegada() {
        setData(LocalDate.now());
        setHorario(LocalTime.now().withNano(0));
    }

    @Override
    public void validarRealizacao() {
        if (getPaciente() == null) throw new RegraNegocioException("O atendimento de emergência deve ter um paciente.");
        if (prioridade == null) throw new RegraNegocioException("A prioridade do atendimento de emergência é obrigatória.");
        if (getProfissional() != null && !(getProfissional() instanceof Medico)) {
            throw new RegraNegocioException("O atendimento de emergência deve ser conduzido por um médico.");
        }
    }

    /** Emergências são imediatas e não participam do controle de horários agendados. */
    @Override
    public boolean ocupaAgenda() { return false; }

    public void iniciar(ProfissionalSaude medico) {
        if (status != StatusEmergencia.AGUARDANDO_AVALIACAO) {
            throw new RegraNegocioException("Apenas atendimentos aguardando avaliação podem ser iniciados (situação atual: " + status + ").");
        }
        if (medico == null) throw new RegraNegocioException("Informe o médico que iniciará o atendimento.");
        setProfissional(medico);
        validarRealizacao();
        this.status = StatusEmergencia.EM_ATENDIMENTO;
    }

    public void concluir(String observacoes) {
        if (status != StatusEmergencia.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Apenas atendimentos em andamento podem ser concluídos (situação atual: " + status + ").");
        }
        if (observacoes != null && !observacoes.isBlank()) setObservacoes(observacoes);
        this.status = StatusEmergencia.CONCLUIDO;
    }

    public void reclassificar(PrioridadeEmergencia nova) {
        if (status == StatusEmergencia.CONCLUIDO) throw new RegraNegocioException("Atendimento concluído não pode ser reclassificado.");
        this.prioridade = nova;
    }

    @Override
    public String getModalidade() { return "EMERGENCIA"; }

    public PrioridadeEmergencia getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeEmergencia prioridade) { this.prioridade = prioridade; }
    public StatusEmergencia getStatus() { return status; }
    public String getQueixa() { return queixa; }
    public void setQueixa(String queixa) { this.queixa = queixa; }
}
