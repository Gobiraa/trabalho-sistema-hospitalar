package br.com.hospital.model;

import br.com.hospital.exception.RegraNegocioException;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "consultas_rotina")
public class ConsultaRotina extends Consulta {

    @Override
    protected void validarProfissional() {
        if (!(getProfissional() instanceof MedicoClinicoGeral)) {
            throw new RegraNegocioException("Consulta de rotina deve ser realizada por um médico clínico geral.");
        }
    }

    @Override
    public String getModalidade() { return "ROTINA"; }
}
