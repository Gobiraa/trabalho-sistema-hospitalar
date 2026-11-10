package br.com.hospital.dto;

import java.util.List;

public record HistoricoDto(PacienteDto.Response paciente,
                           List<ConsultaDto.Response> consultas,
                           List<EmergenciaDto.Response> emergencias,
                           List<ProcedimentoDto.Response> procedimentos,
                           List<InternacaoDto.Response> internacoes,
                           List<String> informacoesRelevantes) {
}
