package br.com.hospital.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.ConsultaDto;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.Consulta;
import br.com.hospital.model.ConsultaEspecializada;
import br.com.hospital.model.ConsultaRotina;
import br.com.hospital.model.MedicoClinicoGeral;
import br.com.hospital.model.MedicoEspecialista;
import br.com.hospital.model.Paciente;
import br.com.hospital.model.ProfissionalSaude;
import br.com.hospital.model.enums.StatusConsulta;
import br.com.hospital.repository.ConsultaRepository;
import br.com.hospital.repository.PacienteRepository;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultas;
    private final PacienteRepository pacientes;
    private final ProfissionalService profissionais;
    private final AgendaService agenda;

    public ConsultaService(ConsultaRepository consultas, PacienteRepository pacientes,
                           ProfissionalService profissionais, AgendaService agenda) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.agenda = agenda;
    }

    public ConsultaDto.Response agendar(ConsultaDto.Request req) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));
        ProfissionalSaude profissional = profissionais.obter(req.profissionalId());

        Consulta consulta;
        if (req.tipo() == ConsultaDto.Tipo.ESPECIALIZADA) {
            ConsultaEspecializada ce = new ConsultaEspecializada();
            ce.setEspecialidade(req.especialidade());
            consulta = ce;
        } else {
            if (req.especialidade() != null) {
                throw new RegraNegocioException("Consulta de rotina não possui especialidade.");
            }
            consulta = new ConsultaRotina();
        }
        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setData(req.data());
        consulta.setHorario(req.horario());
        consulta.setMotivo(req.motivo());
        return ConsultaDto.Response.de(salvarNova(consulta));
    }

    /** Médico clínico geral encaminha o paciente para um especialista (gera uma nova consulta especializada). */
    public ConsultaDto.Response encaminhar(Long consultaId, ConsultaDto.Encaminhamento req) {
        Consulta origem = obter(consultaId);
        if (!(origem.getProfissional() instanceof MedicoClinicoGeral)) {
            throw new RegraNegocioException("Somente consultas conduzidas por médico clínico geral permitem encaminhamento.");
        }
        if (origem.getStatus() != StatusConsulta.EM_ATENDIMENTO && origem.getStatus() != StatusConsulta.CONCLUIDA) {
            throw new RegraNegocioException("O encaminhamento só pode ser feito durante ou após o atendimento.");
        }
        ProfissionalSaude destino = profissionais.obter(req.profissionalId());
        if (!(destino instanceof MedicoEspecialista esp)) {
            throw new RegraNegocioException("O encaminhamento deve ser feito para um médico especialista.");
        }
        ConsultaEspecializada nova = new ConsultaEspecializada();
        nova.setPaciente(origem.getPaciente());
        nova.setProfissional(esp);
        nova.setEspecialidade(esp.getEspecialidade());
        nova.setData(req.data());
        nova.setHorario(req.horario());
        nova.setMotivo(req.motivo());
        return ConsultaDto.Response.de(salvarNova(nova));
    }

    @Transactional(readOnly = true)
    public List<ConsultaDto.Response> listar(Long pacienteId, Long profissionalId, StatusConsulta status) {
        Specification<Consulta> spec = (root, q, cb) -> cb.conjunction();
        if (pacienteId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("paciente").get("id"), pacienteId));
        if (profissionalId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("profissional").get("id"), profissionalId));
        if (status != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), status));
        return consultas.findAll(spec).stream()
                .sorted(java.util.Comparator.comparing(Consulta::getData).thenComparing(Consulta::getHorario))
                .map(ConsultaDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaDto.Response buscar(Long id) {
        return ConsultaDto.Response.de(obter(id));
    }

    public ConsultaDto.Response reagendar(Long id, ConsultaDto.Atualizacao req) {
        Consulta c = obter(id);
        c.validarPodeSerAlterada();
        c.setProfissional(profissionais.obter(req.profissionalId()));
        c.setData(req.data());
        c.setHorario(req.horario());
        c.setMotivo(req.motivo());
        if (c instanceof ConsultaEspecializada ce) {
            ce.setEspecialidade(req.especialidade());
        } else if (req.especialidade() != null) {
            throw new RegraNegocioException("Consulta de rotina não possui especialidade.");
        }
        c.validarRealizacao();
        c.validarAgendamentoFuturo();
        agenda.garantirDisponibilidade(req.profissionalId(), req.data(), req.horario(), id);
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response confirmar(Long id) {
        Consulta c = obter(id);
        c.confirmar();
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response iniciar(Long id) {
        Consulta c = obter(id);
        c.iniciarAtendimento();
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response concluir(Long id, String observacoes) {
        Consulta c = obter(id);
        c.concluir(observacoes);
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response cancelar(Long id) {
        Consulta c = obter(id);
        c.cancelar();
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response naoCompareceu(Long id) {
        Consulta c = obter(id);
        c.registrarNaoComparecimento();
        return ConsultaDto.Response.de(c);
    }

    public ConsultaDto.Response registrarObservacoes(Long id, String observacoes) {
        Consulta c = obter(id);
        c.registrarObservacoes(observacoes);
        return ConsultaDto.Response.de(c);
    }

    private Consulta salvarNova(Consulta consulta) {
        consulta.validarRealizacao();
        consulta.validarAgendamentoFuturo();
        agenda.garantirDisponibilidade(consulta.getProfissional().getId(), consulta.getData(), consulta.getHorario(), null);
        return consultas.save(consulta);
    }

    private Consulta obter(Long id) {
        return consultas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }
}
