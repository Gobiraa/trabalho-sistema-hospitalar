package br.com.hospital.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hospital.dto.QuartoDto;
import br.com.hospital.model.enums.StatusQuarto;
import br.com.hospital.model.enums.TipoQuarto;
import br.com.hospital.service.QuartoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuartoDto.Response> criar(@Valid @RequestBody QuartoDto.Request req) {
        var r = service.criar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    @GetMapping
    public List<QuartoDto.Response> listar(@RequestParam(required = false) StatusQuarto status,
                                           @RequestParam(required = false) TipoQuarto tipo) {
        return service.listar(status, tipo);
    }

    @GetMapping("/{id}")
    public QuartoDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public QuartoDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody QuartoDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
