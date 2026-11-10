package br.com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.Quarto;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    boolean existsByNumero(int numero);
}
