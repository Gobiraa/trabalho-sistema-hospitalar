package br.com.hospital.model;

import java.time.LocalDate;

import br.com.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "internacoes")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "profissional_id")
    private Medico profissional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    private LocalDate dataEntrada;
    private LocalDate dataPrevistaAlta;
    private LocalDate dataEfetivaAlta;

    @Column(length = 4000)
    private String observacoes;

    public boolean isAtiva() { return dataEfetivaAlta == null; }

    public void validarDatas() {
        if (dataEntrada == null) throw new RegraNegocioException("A data de entrada é obrigatória.");
        if (dataPrevistaAlta != null && dataPrevistaAlta.isBefore(dataEntrada)) {
            throw new RegraNegocioException("A data prevista de alta não pode ser anterior à data de entrada.");
        }
    }

    public void registrarAlta(LocalDate data) {
        if (!isAtiva()) throw new RegraNegocioException("A internação já recebeu alta em " + dataEfetivaAlta + ".");
        if (data.isBefore(dataEntrada)) throw new RegraNegocioException("A data de alta não pode ser anterior à data de entrada.");
        this.dataEfetivaAlta = data;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Medico getProfissional() { return profissional; }
    public void setProfissional(Medico profissional) { this.profissional = profissional; }
    public Quarto getQuarto() { return quarto; }
    public void setQuarto(Quarto quarto) { this.quarto = quarto; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }
    public LocalDate getDataPrevistaAlta() { return dataPrevistaAlta; }
    public void setDataPrevistaAlta(LocalDate dataPrevistaAlta) { this.dataPrevistaAlta = dataPrevistaAlta; }
    public LocalDate getDataEfetivaAlta() { return dataEfetivaAlta; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
