package br.com.hospital.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.InternacaoDto;
import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.model.Internacao;
import br.com.hospital.repository.InternacaoRepository;
import br.com.hospital.repository.MedicoRepository;
import br.com.hospital.repository.PacienteRepository;

@Service
@Transactional
public class InternacaoService {

    private final InternacaoRepository internacoes;
    private final PacienteRepository pacientes;
    private final MedicoRepository medicos;
    private final QuartoService quartos;

    public InternacaoService(InternacaoRepository internacoes, PacienteRepository pacientes,
                             MedicoRepository medicos, QuartoService quartos) {
        this.internacoes = internacoes;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.quartos = quartos;
    }

    public InternacaoDto.Response internar(InternacaoDto.Request req) {
        if (internacoes.existsByPacienteIdAndDataEfetivaAltaIsNull(req.pacienteId())) {
            throw new ConflitoException("O paciente já possui uma internação ativa.");
        }
        Internacao i = new Internacao();
        i.setPaciente(pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId())));
        i.setProfissional(medicos.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico responsável", req.profissionalId())));
        i.setDataEntrada(req.dataEntrada());
        i.setDataPrevistaAlta(req.dataPrevistaAlta());
        i.setObservacoes(req.observacoes());
        i.validarDatas();

        var quarto = quartos.obter(req.quartoId());
        quarto.ocuparVaga(); // lança conflito se o quarto estiver lotado (regras 4 e 5)
        i.setQuarto(quarto);
        return InternacaoDto.Response.de(internacoes.save(i));
    }

    @Transactional(readOnly = true)
    public List<InternacaoDto.Response> listar(Long pacienteId, Boolean ativa) {
        return internacoes.findAll().stream()
                .filter(i -> pacienteId == null || i.getPaciente().getId().equals(pacienteId))
                .filter(i -> ativa == null || i.isAtiva() == ativa)
                .map(InternacaoDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public InternacaoDto.Response buscar(Long id) {
        return InternacaoDto.Response.de(obter(id));
    }

    /** Atualiza data prevista de alta e observações de uma internação ainda ativa. */
    public InternacaoDto.Response atualizar(Long id, InternacaoDto.Atualizacao req) {
        Internacao i = obter(id);
        if (!i.isAtiva()) throw new br.com.hospital.exception.RegraNegocioException("Internação encerrada não pode ser alterada.");
        i.setDataPrevistaAlta(req.dataPrevistaAlta());
        i.setObservacoes(req.observacoes());
        i.validarDatas();
        return InternacaoDto.Response.de(i);
    }

    public InternacaoDto.Response registrarAlta(Long id, LocalDate dataAlta) {
        Internacao i = obter(id);
        i.registrarAlta(dataAlta != null ? dataAlta : LocalDate.now());
        i.getQuarto().liberarVaga();
        return InternacaoDto.Response.de(i);
    }

    private Internacao obter(Long id) {
        return internacoes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Internação", id));
    }
}
