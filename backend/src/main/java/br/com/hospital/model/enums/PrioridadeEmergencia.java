package br.com.hospital.model.enums;

public enum PrioridadeEmergencia {
    BAIXA(1), MODERADA(2), ALTA(3), CRITICA(4);

    private final int nivel;

    PrioridadeEmergencia(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }
}
