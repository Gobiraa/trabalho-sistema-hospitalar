package br.com.hospital.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusProcedimento {
    AGENDADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO;

    public Set<StatusProcedimento> proximosPermitidos() {
        return switch (this) {
            case AGENDADO -> EnumSet.of(EM_ANDAMENTO, CANCELADO);
            case EM_ANDAMENTO -> EnumSet.of(CONCLUIDO, CANCELADO);
            case CONCLUIDO, CANCELADO -> EnumSet.noneOf(StatusProcedimento.class);
        };
    }

    public boolean podeIrPara(StatusProcedimento destino) {
        return proximosPermitidos().contains(destino);
    }

    public boolean ocupaAgenda() {
        return this == AGENDADO || this == EM_ANDAMENTO;
    }
}
