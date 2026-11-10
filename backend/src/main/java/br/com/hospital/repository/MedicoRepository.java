package br.com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
}
