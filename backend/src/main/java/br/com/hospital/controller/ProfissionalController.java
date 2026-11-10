package br.com.hospital.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.hospital.dto.ProcedimentoDto;
import br.com.hospital.dto.ProfissionalDto;
import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.Cirurgiao;
import br.com.hospital.model.enums.Especialidade;
import br.com.hospital.service.ProcedimentoService;
import br.com.hospital.service.ProfissionalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;
    private final ProcedimentoService procedimentos;

    public ProfissionalController(ProfissionalService service, ProcedimentoService procedimentos) {
        this.service = service;
        this.procedimentos = procedimentos;
    }

    @PostMapping
    public ResponseEntity<ProfissionalDto.Response> criar(@Valid @RequestBody ProfissionalDto.Request req) {
        var r = service.criar(req);
        return ResponseEntity.created(Uris.recurso(r.id())).body(r);
    }

    /** Filtros opcionais: especialidade e (data + horario) para listar apenas os disponíveis. */
    @GetMapping
    public List<ProfissionalDto.Response> listar(
            @RequestParam(required = false) Especialidade especialidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horario) {
        return service.listar(especialidade, data, horario);
    }

    @GetMapping("/{id}")
    public ProfissionalDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ProfissionalDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalDto.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/disponibilidade")
    public ProfissionalDto.Disponibilidade disponibilidade(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horario) {
        return service.disponibilidade(id, data, horario);
    }

    /** Procedimentos cirúrgicos registrados para o cirurgião (data, paciente e descrição). */
    @GetMapping("/{id}/procedimentos")
    public List<ProcedimentoDto.Response> procedimentos(@PathVariable Long id) {
        if (!(service.obter(id) instanceof Cirurgiao)) {
            throw new RegraNegocioException("Somente cirurgiões possuem procedimentos cirúrgicos.");
        }
        return procedimentos.listar(null, id, null);
    }
}
