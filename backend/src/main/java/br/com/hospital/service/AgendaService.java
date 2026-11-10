package br.com.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.exception.ConflitoException;
import br.com.hospital.model.Atendimento;
import br.com.hospital.repository.AtendimentoRepository;

/** Regra 3: um profissional não pode ter dois atendimentos agendados no mesmo horário. */
@Service
public class AgendaService {

    private final AtendimentoRepository atendimentos;

    public AgendaService(AtendimentoRepository atendimentos) {
        this.atendimentos = atendimentos;
    }

    @Transactional(readOnly = true)
    public boolean estaDisponivel(Long profissionalId, LocalDate data, LocalTime horario) {
        return conflitante(profissionalId, data, horario, null) == null;
    }

    /** @param ignorarId atendimento a desconsiderar (usado em reagendamentos) */
    @Transactional(readOnly = true)
    public void garantirDisponibilidade(Long profissionalId, LocalDate data, LocalTime horario, Long ignorarId) {
        Atendimento conflito = conflitante(profissionalId, data, horario, ignorarId);
        if (conflito != null) {
            throw new ConflitoException("O profissional já possui um atendimento (" + conflito.getModalidade()
                    + ") agendado para " + data + " às " + horario + ".");
        }
    }

    private Atendimento conflitante(Long profissionalId, LocalDate data, LocalTime horario, Long ignorarId) {
        return atendimentos.findByProfissionalIdAndDataAndHorario(profissionalId, data, horario).stream()
                .filter(Atendimento::ocupaAgenda)
                .filter(a -> !a.getId().equals(ignorarId))
                .findFirst()
                .orElse(null);
    }
}
