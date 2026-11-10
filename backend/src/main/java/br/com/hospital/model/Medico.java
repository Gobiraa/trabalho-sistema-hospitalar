package br.com.hospital.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Informações e comportamentos comuns a todos os médicos. */
@Entity
@Table(name = "medicos")
public abstract class Medico extends ProfissionalSaude {
}
