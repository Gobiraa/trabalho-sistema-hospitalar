package br.com.hospital.dto;

import br.com.hospital.model.Cirurgiao;
import br.com.hospital.model.MedicoEspecialista;
import br.com.hospital.model.ProfissionalSaude;
import br.com.hospital.model.enums.Especialidade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProfissionalDto {
    private ProfissionalDto() {}

    public enum Categoria { CLINICO_GERAL, ESPECIALISTA, CIRURGIAO }

    public record Request(
            @NotNull(message = "Categoria é obrigatória") Categoria categoria,
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "Registro profissional é obrigatório") String registroProfissional,
            @NotBlank(message = "Telefone é obrigatório") String telefone,
            @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
            Especialidade especialidade,
            String areaCirurgica) {
    }

    public record Response(Long id, String categoria, String nome, String registroProfissional, String telefone,
                           String email, Especialidade especialidade, String areaCirurgica, String atribuicoes) {
        public static Response de(ProfissionalSaude p) {
            Especialidade esp = p instanceof MedicoEspecialista e ? e.getEspecialidade() : null;
            String area = p instanceof Cirurgiao c ? c.getAreaCirurgica() : null;
            return new Response(p.getId(), p.getCategoria(), p.getNome(), p.getRegistroProfissional(), p.getTelefone(),
                    p.getEmail(), esp, area, p.descreverAtribuicoes());
        }
    }

    public record Disponibilidade(Long profissionalId, java.time.LocalDate data, java.time.LocalTime horario, boolean disponivel) {
    }
}
