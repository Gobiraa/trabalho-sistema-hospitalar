package br.com.hospital.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hospital.dto.ConsultaDto;
import br.com.hospital.dto.ObservacoesRequest;
import br.com.hospital.model.enums.StatusConsulta;
import br.com.hospital.service.ConsultaService;
import jakarta.validation.Valid;

/**
 * Consultas não possuem DELETE: o histórico deve ser mantido (regra 6).
 * Para desfazer um agendamento use POST /consultas/{id}/cancelar.
 */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConsultaDto.Response> agendar(@Valid @RequestBody ConsultaDto.Request req) {
        var r = service.agendar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    @GetMapping
    public List<ConsultaDto.Response> listar(@RequestParam(required = false) Long pacienteId,
                                             @RequestParam(required = false) Long profissionalId,
                                             @RequestParam(required = false) StatusConsulta status) {
        return service.listar(pacienteId, profissionalId, status);
    }

    @GetMapping("/{id}")
    public ConsultaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ConsultaDto.Response reagendar(@PathVariable Long id, @Valid @RequestBody ConsultaDto.Atualizacao req) {
        return service.reagendar(id, req);
    }

    @PostMapping("/{id}/confirmar")
    public ConsultaDto.Response confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PostMapping("/{id}/iniciar")
    public ConsultaDto.Response iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/{id}/concluir")
    public ConsultaDto.Response concluir(@PathVariable Long id, @RequestBody(required = false) ObservacoesRequest req) {
        return service.concluir(id, req == null ? null : req.observacoes());
    }

    @PostMapping("/{id}/cancelar")
    public ConsultaDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PostMapping("/{id}/nao-compareceu")
    public ConsultaDto.Response naoCompareceu(@PathVariable Long id) {
        return service.naoCompareceu(id);
    }

    @PatchMapping("/{id}/observacoes")
    public ConsultaDto.Response observacoes(@PathVariable Long id, @Valid @RequestBody ObservacoesRequest req) {
        return service.registrarObservacoes(id, req.observacoes());
    }

    @PostMapping("/{id}/encaminhamento")
    public ResponseEntity<ConsultaDto.Response> encaminhar(@PathVariable Long id,
                                                           @Valid @RequestBody ConsultaDto.Encaminhamento req) {
        var r = service.encaminhar(id, req);
        return ResponseEntity.created(java.net.URI.create("/consultas/" + r.id())).body(r);
    }
}
