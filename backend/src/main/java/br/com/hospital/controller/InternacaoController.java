package br.com.hospital.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hospital.dto.InternacaoDto;
import br.com.hospital.service.InternacaoService;
import jakarta.validation.Valid;

/** Internações não possuem DELETE: o histórico deve ser mantido (regra 6). */
@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InternacaoDto.Response> internar(@Valid @RequestBody InternacaoDto.Request req) {
        var r = service.internar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    @GetMapping
    public List<InternacaoDto.Response> listar(@RequestParam(required = false) Long pacienteId,
                                               @RequestParam(required = false) Boolean ativa) {
        return service.listar(pacienteId, ativa);
    }

    @GetMapping("/{id}")
    public InternacaoDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public InternacaoDto.Response atualizar(@PathVariable Long id, @RequestBody InternacaoDto.Atualizacao req) {
        return service.atualizar(id, req);
    }

    @PostMapping("/{id}/alta")
    public InternacaoDto.Response alta(@PathVariable Long id, @RequestBody(required = false) InternacaoDto.Alta req) {
        return service.registrarAlta(id, req == null ? null : req.dataAlta());
    }
}
