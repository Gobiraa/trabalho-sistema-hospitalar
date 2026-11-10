package br.com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.Internacao;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {
    boolean existsByPacienteIdAndDataEfetivaAltaIsNull(Long pacienteId);

    List<Internacao> findByPacienteIdOrderByDataEntradaAsc(Long pacienteId);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByProfissionalId(Long profissionalId);

    boolean existsByQuartoId(Long quartoId);
}
