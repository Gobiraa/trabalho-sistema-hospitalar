package br.com.hospital.exception;

/** Conflito com o estado atual dos dados: horário ocupado, duplicidade, quarto lotado (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
