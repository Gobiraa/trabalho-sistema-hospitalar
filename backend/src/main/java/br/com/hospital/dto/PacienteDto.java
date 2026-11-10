package br.com.hospital.dto;

import java.time.LocalDate;

import br.com.hospital.model.Paciente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public final class PacienteDto {
    private PacienteDto() {}

    public record Request(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "CPF é obrigatório") String cpf,
            @NotNull(message = "Data de nascimento é obrigatória") @Past(message = "Data de nascimento deve estar no passado") LocalDate dataNascimento,
            @NotBlank(message = "Telefone é obrigatório") String telefone,
            @NotBlank(message = "Endereço é obrigatório") String endereco,
            @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email) {
    }

    public record Response(Long id, String nome, String cpf, LocalDate dataNascimento, String telefone,
                           String endereco, String email) {
        public static Response de(Paciente p) {
            return new Response(p.getId(), p.getNome(), p.getCpf(), p.getDataNascimento(), p.getTelefone(),
                    p.getEndereco(), p.getEmail());
        }
    }
}
