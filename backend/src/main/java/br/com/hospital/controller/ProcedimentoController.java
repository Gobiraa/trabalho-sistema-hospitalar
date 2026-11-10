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

import br.com.hospital.dto.ProcedimentoDto;
import br.com.hospital.model.enums.StatusProcedimento;
import br.com.hospital.service.ProcedimentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/procedimentos-cirurgicos")
public class ProcedimentoController {

    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcedimentoDto.Response> agendar(@Valid @RequestBody ProcedimentoDto.Request req) {
        var r = service.agendar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    @GetMapping
    public List<ProcedimentoDto.Response> listar(@RequestParam(required = false) Long pacienteId,
                                                 @RequestParam(required = false) Long cirurgiaoId,
                                                 @RequestParam(required = false) StatusProcedimento status) {
        return service.listar(pacienteId, cirurgiaoId, status);
    }

    @GetMapping("/{id}")
    public ProcedimentoDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ProcedimentoDto.Response reagendar(@PathVariable Long id, @Valid @RequestBody ProcedimentoDto.Atualizacao req) {
        return service.reagendar(id, req);
    }

    @PostMapping("/{id}/iniciar")
    public ProcedimentoDto.Response iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/{id}/concluir")
    public ProcedimentoDto.Response concluir(@PathVariable Long id, @RequestBody(required = false) ProcedimentoDto.Conclusao req) {
        return service.concluir(id, req == null ? null : req.observacoesPosOperatorias());
    }

    @PostMapping("/{id}/cancelar")
    public ProcedimentoDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
