package br.com.hospital.dto;

import jakarta.validation.constraints.NotBlank;

public record ObservacoesRequest(@NotBlank(message = "Observações são obrigatórias") String observacoes) {
}
