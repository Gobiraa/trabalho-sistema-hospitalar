package br.com.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/** Raiz da hierarquia de profissionais. Cada categoria define suas próprias atribuições. */
@Entity
@Table(name = "profissionais")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProfissionalSaude extends Pessoa {

    @Column(unique = true, nullable = false)
    private String registroProfissional;

    public String getRegistroProfissional() { return registroProfissional; }
    public void setRegistroProfissional(String registroProfissional) { this.registroProfissional = registroProfissional; }

    /** Identificador da categoria exposto pela API (CLINICO_GERAL, ESPECIALISTA, CIRURGIAO). */
    public abstract String getCategoria();

    /** Descrição das atribuições da categoria. */
    public abstract String descreverAtribuicoes();
}
