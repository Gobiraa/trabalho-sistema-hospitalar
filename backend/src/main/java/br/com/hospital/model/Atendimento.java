package br.com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Atributos e comportamentos comuns às modalidades de atendimento.
 * Cada modalidade implementa sua própria validação de realização.
 */
@Entity
@Table(name = "atendimentos")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    /** Opcional apenas para emergências ainda aguardando avaliação. */
    /** EAGER de propósito: as validações usam instanceof, que falha em proxies lazy do Hibernate. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id")
    private ProfissionalSaude profissional;

    private LocalDate data;
    private LocalTime horario;

    @Column(length = 4000)
    private String observacoes;

    /** Verifica se as condições específicas da modalidade estão satisfeitas. */
    public abstract void validarRealizacao();

    /** Indica se o atendimento ocupa o horário do profissional (regra 3). */
    public abstract boolean ocupaAgenda();

    /** Nome curto da modalidade, exposto pela API. */
    public abstract String getModalidade();

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public ProfissionalSaude getProfissional() { return profissional; }
    public void setProfissional(ProfissionalSaude profissional) { this.profissional = profissional; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
