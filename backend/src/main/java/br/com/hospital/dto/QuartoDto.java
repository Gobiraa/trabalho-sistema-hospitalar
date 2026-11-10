package br.com.hospital.dto;

import br.com.hospital.model.Quarto;
import br.com.hospital.model.enums.StatusQuarto;
import br.com.hospital.model.enums.TipoQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class QuartoDto {
    private QuartoDto() {}

    public record Request(
            @Min(value = 1, message = "Número deve ser positivo") int numero,
            @Min(value = 0, message = "Andar não pode ser negativo") int andar,
            @Min(value = 1, message = "Capacidade deve ser de pelo menos 1") int capacidadeMaxima,
            @NotNull(message = "Tipo é obrigatório") TipoQuarto tipo) {
    }

    public record Response(Long id, int numero, int andar, TipoQuarto tipo, int capacidadeMaxima, int ocupacaoAtual,
                           StatusQuarto status) {
        public static Response de(Quarto q) {
            return new Response(q.getId(), q.getNumero(), q.getAndar(), q.getTipo(), q.getCapacidadeMaxima(),
                    q.getOcupacaoAtual(), q.getStatus());
        }
    }
}
