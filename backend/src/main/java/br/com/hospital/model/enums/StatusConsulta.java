package br.com.hospital.model.enums;

import java.util.EnumSet;
import java.util.Set;

/** Situações de uma consulta e as transições permitidas entre elas. */
public enum StatusConsulta {
    AGENDADA, CONFIRMADA, EM_ATENDIMENTO, CONCLUIDA, CANCELADA, NAO_COMPARECEU;

    public Set<StatusConsulta> proximosPermitidos() {
        return switch (this) {
            case AGENDADA -> EnumSet.of(CONFIRMADA, CANCELADA, NAO_COMPARECEU);
            case CONFIRMADA -> EnumSet.of(EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU);
            case EM_ATENDIMENTO -> EnumSet.of(CONCLUIDA);
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> EnumSet.noneOf(StatusConsulta.class);
        };
    }

    public boolean podeIrPara(StatusConsulta destino) {
        return proximosPermitidos().contains(destino);
    }

    public boolean isFinal() {
        return proximosPermitidos().isEmpty();
    }

    /** Consultas que ainda ocupam a agenda do profissional. */
    public boolean ocupaAgenda() {
        return this == AGENDADA || this == CONFIRMADA || this == EM_ATENDIMENTO;
    }
}
