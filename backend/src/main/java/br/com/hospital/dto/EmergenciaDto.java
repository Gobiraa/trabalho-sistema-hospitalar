package br.com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.hospital.model.AtendimentoEmergencia;
import br.com.hospital.model.enums.PrioridadeEmergencia;
import br.com.hospital.model.enums.StatusEmergencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class EmergenciaDto {
    private EmergenciaDto() {}

    public record Request(
            @NotNull(message = "Paciente é obrigatório") Long pacienteId,
            @NotNull(message = "Prioridade é obrigatória") PrioridadeEmergencia prioridade,
            @NotBlank(message = "Queixa é obrigatória") String queixa) {
    }

    public record Inicio(@NotNull(message = "Médico é obrigatório") Long profissionalId) {
    }

    public record Reclassificacao(@NotNull(message = "Prioridade é obrigatória") PrioridadeEmergencia prioridade) {
    }

    public record Response(Long id, Long pacienteId, String pacienteNome, Long profissionalId, String profissionalNome,
                           PrioridadeEmergencia prioridade, StatusEmergencia status, String queixa, LocalDate data,
                           LocalTime horario, String observacoes) {
        public static Response de(AtendimentoEmergencia e) {
            var prof = e.getProfissional();
            return new Response(e.getId(), e.getPaciente().getId(), e.getPaciente().getNome(),
                    prof == null ? null : prof.getId(), prof == null ? null : prof.getNome(),
                    e.getPrioridade(), e.getStatus(), e.getQueixa(), e.getData(), e.getHorario(), e.getObservacoes());
        }
    }
}
