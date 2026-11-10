package br.com.hospital.model;

import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.StatusQuarto;
import br.com.hospital.model.enums.TipoQuarto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "quartos")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int numero;

    private int andar;
    private int capacidadeMaxima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQuarto tipo;

    private int ocupacaoAtual;

    /** Controle de concorrência: evita que internações simultâneas estourem a capacidade. */
    @Version
    private Long versao;

    public boolean possuiVaga() { return ocupacaoAtual < capacidadeMaxima; }

    public void ocuparVaga() {
        if (!possuiVaga()) {
            throw new ConflitoException("Quarto " + numero + " atingiu sua capacidade máxima (" + capacidadeMaxima + ").");
        }
        ocupacaoAtual++;
    }

    public void liberarVaga() {
        if (ocupacaoAtual > 0) ocupacaoAtual--;
    }

    /** Situação derivada da ocupação: nunca fica inconsistente com ela. */
    @Transient
    public StatusQuarto getStatus() {
        return possuiVaga() ? StatusQuarto.DISPONIVEL : StatusQuarto.OCUPADO;
    }

    /** Valida a capacidade contra o limite do tipo de quarto e a ocupação atual. */
    public void validarCapacidade() {
        if (tipo == null) throw new RegraNegocioException("O tipo do quarto é obrigatório.");
        if (capacidadeMaxima < 1) throw new RegraNegocioException("A capacidade máxima deve ser de pelo menos 1.");
        if (capacidadeMaxima > tipo.getCapacidadeMaximaPermitida()) {
            throw new RegraNegocioException("Quartos do tipo " + tipo + " comportam no máximo "
                    + tipo.getCapacidadeMaximaPermitida() + " paciente(s).");
        }
        if (capacidadeMaxima < ocupacaoAtual) {
            throw new RegraNegocioException("A nova capacidade é menor que a ocupação atual (" + ocupacaoAtual + ").");
        }
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getAndar() { return andar; }
    public void setAndar(int andar) { this.andar = andar; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(int capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
    public TipoQuarto getTipo() { return tipo; }
    public void setTipo(TipoQuarto tipo) { this.tipo = tipo; }
    public int getOcupacaoAtual() { return ocupacaoAtual; }
}
