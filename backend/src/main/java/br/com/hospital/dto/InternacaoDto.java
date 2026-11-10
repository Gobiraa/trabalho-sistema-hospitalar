package br.com.hospital.dto;

import java.time.LocalDate;

import br.com.hospital.model.Internacao;
import jakarta.validation.constraints.NotNull;

public final class InternacaoDto {
    private InternacaoDto() {}

    public record Request(
            @NotNull(message = "Paciente é obrigatório") Long pacienteId,
            @NotNull(message = "Profissional responsável é obrigatório") Long profissionalId,
            @NotNull(message = "Quarto é obrigatório") Long quartoId,
            @NotNull(message = "Data de entrada é obrigatória") LocalDate dataEntrada,
            LocalDate dataPrevistaAlta,
            String observacoes) {
    }

    public record Atualizacao(LocalDate dataPrevistaAlta, String observacoes) {
    }

    /** Se a data não for informada, usa a data atual. */
    public record Alta(LocalDate dataAlta) {
    }

    public record Response(Long id, Long pacienteId, String pacienteNome, Long profissionalId, String profissionalNome,
                           Long quartoId, int quartoNumero, LocalDate dataEntrada, LocalDate dataPrevistaAlta,
                           LocalDate dataEfetivaAlta, boolean ativa, String observacoes) {
        public static Response de(Internacao i) {
            return new Response(i.getId(), i.getPaciente().getId(), i.getPaciente().getNome(),
                    i.getProfissional().getId(), i.getProfissional().getNome(), i.getQuarto().getId(),
                    i.getQuarto().getNumero(), i.getDataEntrada(), i.getDataPrevistaAlta(), i.getDataEfetivaAlta(),
                    i.isAtiva(), i.getObservacoes());
        }
    }
}
