package br.com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.ProfissionalSaude;

public interface ProfissionalRepository extends JpaRepository<ProfissionalSaude, Long> {
    boolean existsByRegistroProfissional(String registroProfissional);
}
