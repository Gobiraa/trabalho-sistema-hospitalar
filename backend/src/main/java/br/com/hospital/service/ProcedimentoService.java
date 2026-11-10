package br.com.hospital.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.ProcedimentoDto;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.model.ProcedimentoCirurgico;
import br.com.hospital.model.enums.StatusProcedimento;
import br.com.hospital.repository.PacienteRepository;
import br.com.hospital.repository.ProcedimentoRepository;

@Service
@Transactional
public class ProcedimentoService {

    private final ProcedimentoRepository procedimentos;
    private final PacienteRepository pacientes;
    private final ProfissionalService profissionais;
    private final AgendaService agenda;

    public ProcedimentoService(ProcedimentoRepository procedimentos, PacienteRepository pacientes,
                               ProfissionalService profissionais, AgendaService agenda) {
        this.procedimentos = procedimentos;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.agenda = agenda;
    }

    public ProcedimentoDto.Response agendar(ProcedimentoDto.Request req) {
        ProcedimentoCirurgico p = new ProcedimentoCirurgico();
        p.setPaciente(pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId())));
        p.setProfissional(profissionais.obter(req.cirurgiaoId()));
        p.setData(req.data());
        p.setHorario(req.horario());
        p.setDescricao(req.descricao());
        p.setTipoCirurgia(req.tipoCirurgia());
        p.setObservacoesPreOperatorias(req.observacoesPreOperatorias());
        p.validarRealizacao();
        p.validarAgendamentoFuturo();
        agenda.garantirDisponibilidade(req.cirurgiaoId(), req.data(), req.horario(), null);
        return ProcedimentoDto.Response.de(procedimentos.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoDto.Response> listar(Long pacienteId, Long cirurgiaoId, StatusProcedimento status) {
        Specification<ProcedimentoCirurgico> spec = (root, q, cb) -> cb.conjunction();
        if (pacienteId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("paciente").get("id"), pacienteId));
        if (cirurgiaoId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("profissional").get("id"), cirurgiaoId));
        if (status != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), status));
        return procedimentos.findAll(spec).stream()
                .sorted(Comparator.comparing(ProcedimentoCirurgico::getData).thenComparing(ProcedimentoCirurgico::getHorario))
                .map(ProcedimentoDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoDto.Response buscar(Long id) {
        return ProcedimentoDto.Response.de(obter(id));
    }

    public ProcedimentoDto.Response reagendar(Long id, ProcedimentoDto.Atualizacao req) {
        ProcedimentoCirurgico p = obter(id);
        p.validarPodeSerAlterado();
        p.setProfissional(profissionais.obter(req.cirurgiaoId()));
        p.setData(req.data());
        p.setHorario(req.horario());
        p.setDescricao(req.descricao());
        p.setTipoCirurgia(req.tipoCirurgia());
        p.setObservacoesPreOperatorias(req.observacoesPreOperatorias());
        p.validarRealizacao();
        p.validarAgendamentoFuturo();
        agenda.garantirDisponibilidade(req.cirurgiaoId(), req.data(), req.horario(), id);
        return ProcedimentoDto.Response.de(p);
    }

    public ProcedimentoDto.Response iniciar(Long id) {
        ProcedimentoCirurgico p = obter(id);
        p.iniciar();
        return ProcedimentoDto.Response.de(p);
    }

    public ProcedimentoDto.Response concluir(Long id, String observacoesPosOperatorias) {
        ProcedimentoCirurgico p = obter(id);
        p.concluir(observacoesPosOperatorias);
        return ProcedimentoDto.Response.de(p);
    }

    public ProcedimentoDto.Response cancelar(Long id) {
        ProcedimentoCirurgico p = obter(id);
        p.cancelar();
        return ProcedimentoDto.Response.de(p);
    }

    private ProcedimentoCirurgico obter(Long id) {
        return procedimentos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico", id));
    }
}
