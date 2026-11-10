package br.com.hospital.model.enums;

/** Tipos de quarto, cada um com um limite próprio de capacidade. */
public enum TipoQuarto {
    ENFERMARIA(6), APARTAMENTO(2), UTI(1), ISOLAMENTO(1);

    private final int capacidadeMaximaPermitida;

    TipoQuarto(int capacidadeMaximaPermitida) {
        this.capacidadeMaximaPermitida = capacidadeMaximaPermitida;
    }

    public int getCapacidadeMaximaPermitida() {
        return capacidadeMaximaPermitida;
    }
}
