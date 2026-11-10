package br.com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.hospital.model.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long>, JpaSpecificationExecutor<Consulta> {
    List<Consulta> findByPacienteIdOrderByDataAscHorarioAsc(Long pacienteId);
}
