package br.com.hospital.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.EmergenciaDto;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.model.AtendimentoEmergencia;
import br.com.hospital.model.enums.PrioridadeEmergencia;
import br.com.hospital.model.enums.StatusEmergencia;
import br.com.hospital.repository.EmergenciaRepository;
import br.com.hospital.repository.PacienteRepository;

@Service
@Transactional
public class EmergenciaService {

    private final EmergenciaRepository emergencias;
    private final PacienteRepository pacientes;
    private final ProfissionalService profissionais;

    public EmergenciaService(EmergenciaRepository emergencias, PacienteRepository pacientes,
                             ProfissionalService profissionais) {
        this.emergencias = emergencias;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public EmergenciaDto.Response registrar(EmergenciaDto.Request req) {
        AtendimentoEmergencia e = new AtendimentoEmergencia();
        e.setPaciente(pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId())));
        e.setPrioridade(req.prioridade());
        e.setQueixa(req.queixa());
        e.registrarChegada();
        e.validarRealizacao();
        return EmergenciaDto.Response.de(emergencias.save(e));
    }

    /** Fila de atendimento: maior prioridade primeiro; em empate, quem chegou antes. */
    @Transactional(readOnly = true)
    public List<EmergenciaDto.Response> listar(StatusEmergencia status) {
        List<AtendimentoEmergencia> lista = status == null ? emergencias.findAll() : emergencias.findByStatus(status);
        return lista.stream()
                .sorted(Comparator.comparingInt((AtendimentoEmergencia e) -> -e.getPrioridade().getNivel())
                        .thenComparing(AtendimentoEmergencia::getData)
                        .thenComparing(AtendimentoEmergencia::getHorario)
                        .thenComparing(AtendimentoEmergencia::getId))
                .map(EmergenciaDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergenciaDto.Response buscar(Long id) {
        return EmergenciaDto.Response.de(obter(id));
    }

    public EmergenciaDto.Response iniciar(Long id, Long profissionalId) {
        AtendimentoEmergencia e = obter(id);
        e.iniciar(profissionais.obter(profissionalId));
        return EmergenciaDto.Response.de(e);
    }

    public EmergenciaDto.Response concluir(Long id, String observacoes) {
        AtendimentoEmergencia e = obter(id);
        e.concluir(observacoes);
        return EmergenciaDto.Response.de(e);
    }

    public EmergenciaDto.Response reclassificar(Long id, PrioridadeEmergencia prioridade) {
        AtendimentoEmergencia e = obter(id);
        e.reclassificar(prioridade);
        return EmergenciaDto.Response.de(e);
    }

    private AtendimentoEmergencia obter(Long id) {
        return emergencias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento de emergência", id));
    }
}
