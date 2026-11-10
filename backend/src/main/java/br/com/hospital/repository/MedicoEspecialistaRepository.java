package br.com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hospital.model.MedicoEspecialista;
import br.com.hospital.model.enums.Especialidade;

public interface MedicoEspecialistaRepository extends JpaRepository<MedicoEspecialista, Long> {
    List<MedicoEspecialista> findByEspecialidade(Especialidade especialidade);
}
