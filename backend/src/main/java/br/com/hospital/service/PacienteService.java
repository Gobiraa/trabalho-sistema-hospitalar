package br.com.hospital.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.ConsultaDto;
import br.com.hospital.dto.EmergenciaDto;
import br.com.hospital.dto.HistoricoDto;
import br.com.hospital.dto.InternacaoDto;
import br.com.hospital.dto.PacienteDto;
import br.com.hospital.dto.ProcedimentoDto;
import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.model.Atendimento;
import br.com.hospital.model.Internacao;
import br.com.hospital.model.Paciente;
import br.com.hospital.repository.AtendimentoRepository;
import br.com.hospital.repository.ConsultaRepository;
import br.com.hospital.repository.EmergenciaRepository;
import br.com.hospital.repository.InternacaoRepository;
import br.com.hospital.repository.PacienteRepository;
import br.com.hospital.repository.ProcedimentoRepository;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacientes;
    private final AtendimentoRepository atendimentos;
    private final InternacaoRepository internacoes;
    private final ConsultaRepository consultas;
    private final EmergenciaRepository emergencias;
    private final ProcedimentoRepository procedimentos;

    public PacienteService(PacienteRepository pacientes, AtendimentoRepository atendimentos,
                           InternacaoRepository internacoes, ConsultaRepository consultas,
                           EmergenciaRepository emergencias, ProcedimentoRepository procedimentos) {
        this.pacientes = pacientes;
        this.atendimentos = atendimentos;
        this.internacoes = internacoes;
        this.consultas = consultas;
        this.emergencias = emergencias;
        this.procedimentos = procedimentos;
    }

    public PacienteDto.Response criar(PacienteDto.Request req) {
        String cpf = Cpf.normalizarEValidar(req.cpf());
        if (pacientes.existsByCpf(cpf)) throw new ConflitoException("Já existe um paciente com este CPF.");
        Paciente p = new Paciente();
        preencher(p, req, cpf);
        return PacienteDto.Response.de(pacientes.save(p));
    }

    @Transactional(readOnly = true)
    public List<PacienteDto.Response> listar(String nome) {
        return pacientes.findAll().stream()
                .filter(p -> nome == null || p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .map(PacienteDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteDto.Response buscar(Long id) {
        return PacienteDto.Response.de(obter(id));
    }

    public PacienteDto.Response atualizar(Long id, PacienteDto.Request req) {
        Paciente p = obter(id);
        String cpf = Cpf.normalizarEValidar(req.cpf());
        if (!cpf.equals(p.getCpf()) && pacientes.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um paciente com este CPF.");
        }
        preencher(p, req, cpf);
        return PacienteDto.Response.de(p);
    }

    /** Pacientes com histórico não podem ser removidos (regra 6). */
    public void remover(Long id) {
        Paciente p = obter(id);
        if (atendimentos.existsByPacienteId(id) || internacoes.existsByPacienteId(id)) {
            throw new ConflitoException("Paciente possui histórico de atendimentos e não pode ser removido.");
        }
        pacientes.delete(p);
    }

    @Transactional(readOnly = true)
    public HistoricoDto historico(Long id) {
        Paciente p = obter(id);
        var listaConsultas = consultas.findByPacienteIdOrderByDataAscHorarioAsc(id);
        var listaEmergencias = emergencias.findByPacienteIdOrderByDataAscHorarioAsc(id);
        var listaProcedimentos = procedimentos.findByPacienteIdOrderByDataAscHorarioAsc(id);
        var listaInternacoes = internacoes.findByPacienteIdOrderByDataEntradaAsc(id);

        List<String> relevantes = new ArrayList<>();
        for (Atendimento a : atendimentos.findByPacienteIdOrderByDataAscHorarioAsc(id)) {
            adicionar(relevantes, a.getData(), a.getModalidade(), a.getObservacoes());
        }
        for (Internacao i : listaInternacoes) {
            adicionar(relevantes, i.getDataEntrada(), "INTERNACAO", i.getObservacoes());
        }

        return new HistoricoDto(PacienteDto.Response.de(p),
                listaConsultas.stream().map(ConsultaDto.Response::de).toList(),
                listaEmergencias.stream().map(EmergenciaDto.Response::de).toList(),
                listaProcedimentos.stream().map(ProcedimentoDto.Response::de).toList(),
                listaInternacoes.stream().map(InternacaoDto.Response::de).toList(),
                relevantes);
    }

    private void adicionar(List<String> lista, Object data, String origem, String obs) {
        if (obs != null && !obs.isBlank()) lista.add(data + " [" + origem + "]: " + obs);
    }

    private void preencher(Paciente p, PacienteDto.Request req, String cpf) {
        p.setNome(req.nome());
        p.setCpf(cpf);
        p.setDataNascimento(req.dataNascimento());
        p.setTelefone(req.telefone());
        p.setEndereco(req.endereco());
        p.setEmail(req.email());
    }

    private Paciente obter(Long id) {
        return pacientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }
}
