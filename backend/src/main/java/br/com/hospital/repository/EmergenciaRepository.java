package br.com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.AtendimentoEmergencia;
import br.com.hospital.model.enums.StatusEmergencia;

public interface EmergenciaRepository extends JpaRepository<AtendimentoEmergencia, Long> {
    List<AtendimentoEmergencia> findByStatus(StatusEmergencia status);

    List<AtendimentoEmergencia> findByPacienteIdOrderByDataAscHorarioAsc(Long pacienteId);
}
