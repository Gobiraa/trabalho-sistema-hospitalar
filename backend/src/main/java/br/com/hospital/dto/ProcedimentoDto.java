package br.com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.hospital.model.ProcedimentoCirurgico;
import br.com.hospital.model.enums.StatusProcedimento;
import br.com.hospital.model.enums.TipoCirurgia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProcedimentoDto {
    private ProcedimentoDto() {}

    public record Request(
            @NotNull(message = "Paciente é obrigatório") Long pacienteId,
            @NotNull(message = "Cirurgião é obrigatório") Long cirurgiaoId,
            @NotNull(message = "Data prevista é obrigatória") LocalDate data,
            @NotNull(message = "Horário previsto é obrigatório") LocalTime horario,
            @NotBlank(message = "Descrição é obrigatória") String descricao,
            @NotNull(message = "Tipo de cirurgia é obrigatório") TipoCirurgia tipoCirurgia,
            String observacoesPreOperatorias) {
    }

    /** Só é possível enquanto o procedimento está agendado. */
    public record Atualizacao(
            @NotNull(message = "Cirurgião é obrigatório") Long cirurgiaoId,
            @NotNull(message = "Data prevista é obrigatória") LocalDate data,
            @NotNull(message = "Horário previsto é obrigatório") LocalTime horario,
            @NotBlank(message = "Descrição é obrigatória") String descricao,
            @NotNull(message = "Tipo de cirurgia é obrigatório") TipoCirurgia tipoCirurgia,
            String observacoesPreOperatorias) {
    }

    public record Conclusao(String observacoesPosOperatorias) {
    }

    public record Response(Long id, Long pacienteId, String pacienteNome, Long cirurgiaoId, String cirurgiaoNome,
                           LocalDate dataPrevista, LocalTime horarioPrevisto, LocalDate dataEfetiva,
                           LocalTime horarioEfetivo, String descricao, TipoCirurgia tipoCirurgia,
                           StatusProcedimento status, String observacoesPreOperatorias,
                           String observacoesPosOperatorias) {
        public static Response de(ProcedimentoCirurgico p) {
            return new Response(p.getId(), p.getPaciente().getId(), p.getPaciente().getNome(),
                    p.getProfissional().getId(), p.getProfissional().getNome(), p.getData(), p.getHorario(),
                    p.getDataEfetiva(), p.getHorarioEfetivo(), p.getDescricao(), p.getTipoCirurgia(), p.getStatus(),
                    p.getObservacoesPreOperatorias(), p.getObservacoesPosOperatorias());
        }
    }
}
