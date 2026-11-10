package br.com.hospital.service;

import br.com.hospital.exception.RegraNegocioException;

/** Normalização e validação de CPF (dígitos verificadores). */
public final class Cpf {
    private Cpf() {}

    public static String normalizarEValidar(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (!valido(digitos)) throw new RegraNegocioException("CPF inválido.");
        return digitos;
    }

    static boolean valido(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1) return false;
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    private static int digito(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
