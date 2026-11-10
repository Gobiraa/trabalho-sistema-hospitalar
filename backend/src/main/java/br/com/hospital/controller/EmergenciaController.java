package br.com.hospital.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hospital.dto.EmergenciaDto;
import br.com.hospital.dto.ObservacoesRequest;
import br.com.hospital.model.enums.StatusEmergencia;
import br.com.hospital.service.EmergenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/emergencias")
public class EmergenciaController {

    private final EmergenciaService service;

    public EmergenciaController(EmergenciaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmergenciaDto.Response> registrar(@Valid @RequestBody EmergenciaDto.Request req) {
        var r = service.registrar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    /** Fila ordenada por prioridade; use ?status=AGUARDANDO_AVALIACAO para ver quem ainda não foi avaliado. */
    @GetMapping
    public List<EmergenciaDto.Response> listar(@RequestParam(required = false) StatusEmergencia status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    public EmergenciaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/iniciar")
    public EmergenciaDto.Response iniciar(@PathVariable Long id, @Valid @RequestBody EmergenciaDto.Inicio req) {
        return service.iniciar(id, req.profissionalId());
    }

    @PostMapping("/{id}/concluir")
    public EmergenciaDto.Response concluir(@PathVariable Long id, @RequestBody(required = false) ObservacoesRequest req) {
        return service.concluir(id, req == null ? null : req.observacoes());
    }

    @PatchMapping("/{id}/prioridade")
    public EmergenciaDto.Response reclassificar(@PathVariable Long id, @Valid @RequestBody EmergenciaDto.Reclassificacao req) {
        return service.reclassificar(id, req.prioridade());
    }
}
