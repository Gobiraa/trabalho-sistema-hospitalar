package br.com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.hospital.model.ProcedimentoCirurgico;

public interface ProcedimentoRepository
        extends JpaRepository<ProcedimentoCirurgico, Long>, JpaSpecificationExecutor<ProcedimentoCirurgico> {
    List<ProcedimentoCirurgico> findByPacienteIdOrderByDataAscHorarioAsc(Long pacienteId);

    List<ProcedimentoCirurgico> findByProfissionalIdOrderByDataAscHorarioAsc(Long profissionalId);
}
