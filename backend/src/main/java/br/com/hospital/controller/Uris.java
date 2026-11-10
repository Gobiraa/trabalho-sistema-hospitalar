package br.com.hospital.controller;

import java.net.URI;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

final class Uris {
    private Uris() {}

    /** URI do recurso recém-criado: caminho da requisição atual + "/{id}". */
    static URI recurso(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
