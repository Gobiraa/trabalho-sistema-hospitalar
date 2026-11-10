package br.com.hospital.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.Atendimento;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    List<Atendimento> findByProfissionalIdAndDataAndHorario(Long profissionalId, LocalDate data, LocalTime horario);

    List<Atendimento> findByPacienteIdOrderByDataAscHorarioAsc(Long pacienteId);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByProfissionalId(Long profissionalId);
}
