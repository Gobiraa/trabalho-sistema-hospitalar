package br.com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.hospital.model.Consulta;
import br.com.hospital.model.ConsultaEspecializada;
import br.com.hospital.model.enums.Especialidade;
import br.com.hospital.model.enums.StatusConsulta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ConsultaDto {
    private ConsultaDto() {}

    public enum Tipo { ROTINA, ESPECIALIZADA }

    public record Request(
            @NotNull(message = "Tipo é obrigatório") Tipo tipo,
            @NotNull(message = "Paciente é obrigatório") Long pacienteId,
            @NotNull(message = "Profissional é obrigatório") Long profissionalId,
            @NotNull(message = "Data é obrigatória") LocalDate data,
            @NotNull(message = "Horário é obrigatório") LocalTime horario,
            @NotBlank(message = "Motivo é obrigatório") String motivo,
            Especialidade especialidade) {
    }

    /** Reagendamento/alteração: paciente e tipo da consulta não mudam. */
    public record Atualizacao(
            @NotNull(message = "Profissional é obrigatório") Long profissionalId,
            @NotNull(message = "Data é obrigatória") LocalDate data,
            @NotNull(message = "Horário é obrigatório") LocalTime horario,
            @NotBlank(message = "Motivo é obrigatório") String motivo,
            Especialidade especialidade) {
    }

    public record Encaminhamento(
            @NotNull(message = "Profissional especialista é obrigatório") Long profissionalId,
            @NotNull(message = "Data é obrigatória") LocalDate data,
            @NotNull(message = "Horário é obrigatório") LocalTime horario,
            @NotBlank(message = "Motivo é obrigatório") String motivo) {
    }

    public record Response(Long id, String modalidade, Long pacienteId, String pacienteNome, Long profissionalId,
                           String profissionalNome, LocalDate data, LocalTime horario, String motivo,
                           Especialidade especialidade, StatusConsulta status, String observacoes) {
        public static Response de(Consulta c) {
            Especialidade esp = c instanceof ConsultaEspecializada e ? e.getEspecialidade() : null;
            return new Response(c.getId(), c.getModalidade(), c.getPaciente().getId(), c.getPaciente().getNome(),
                    c.getProfissional().getId(), c.getProfissional().getNome(), c.getData(), c.getHorario(),
                    c.getMotivo(), esp, c.getStatus(), c.getObservacoes());
        }
    }
}
