package br.com.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.ProfissionalDto;
import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.Cirurgiao;
import br.com.hospital.model.MedicoClinicoGeral;
import br.com.hospital.model.MedicoEspecialista;
import br.com.hospital.model.ProfissionalSaude;
import br.com.hospital.model.enums.Especialidade;
import br.com.hospital.repository.AtendimentoRepository;
import br.com.hospital.repository.InternacaoRepository;
import br.com.hospital.repository.MedicoEspecialistaRepository;
import br.com.hospital.repository.ProfissionalRepository;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalRepository profissionais;
    private final MedicoEspecialistaRepository especialistas;
    private final AtendimentoRepository atendimentos;
    private final InternacaoRepository internacoes;
    private final AgendaService agenda;

    public ProfissionalService(ProfissionalRepository profissionais, MedicoEspecialistaRepository especialistas,
                               AtendimentoRepository atendimentos, InternacaoRepository internacoes,
                               AgendaService agenda) {
        this.profissionais = profissionais;
        this.especialistas = especialistas;
        this.atendimentos = atendimentos;
        this.internacoes = internacoes;
        this.agenda = agenda;
    }

    public ProfissionalDto.Response criar(ProfissionalDto.Request req) {
        if (profissionais.existsByRegistroProfissional(req.registroProfissional())) {
            throw new ConflitoException("Já existe um profissional com este registro.");
        }
        ProfissionalSaude p = switch (req.categoria()) {
            case CLINICO_GERAL -> new MedicoClinicoGeral();
            case ESPECIALISTA -> new MedicoEspecialista();
            case CIRURGIAO -> new Cirurgiao();
        };
        preencher(p, req);
        return ProfissionalDto.Response.de(profissionais.save(p));
    }

    /**
     * Lista profissionais, opcionalmente por especialidade. Se data e horário forem informados,
     * retorna somente os disponíveis naquele horário.
     */
    @Transactional(readOnly = true)
    public List<ProfissionalDto.Response> listar(Especialidade especialidade, LocalDate data, LocalTime horario) {
        if ((data == null) != (horario == null)) {
            throw new RegraNegocioException("Informe data e horário juntos para filtrar por disponibilidade.");
        }
        List<? extends ProfissionalSaude> base = especialidade != null
                ? especialistas.findByEspecialidade(especialidade)
                : profissionais.findAll();
        return base.stream()
                .filter(p -> data == null || agenda.estaDisponivel(p.getId(), data, horario))
                .map(ProfissionalDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalDto.Response buscar(Long id) {
        return ProfissionalDto.Response.de(obter(id));
    }

    @Transactional(readOnly = true)
    public ProfissionalDto.Disponibilidade disponibilidade(Long id, LocalDate data, LocalTime horario) {
        obter(id);
        return new ProfissionalDto.Disponibilidade(id, data, horario, agenda.estaDisponivel(id, data, horario));
    }

    /** A categoria não pode ser alterada após o cadastro. */
    public ProfissionalDto.Response atualizar(Long id, ProfissionalDto.Request req) {
        ProfissionalSaude p = obter(id);
        if (!p.getCategoria().equals(req.categoria().name())) {
            throw new RegraNegocioException("A categoria do profissional não pode ser alterada.");
        }
        if (!req.registroProfissional().equals(p.getRegistroProfissional())
                && profissionais.existsByRegistroProfissional(req.registroProfissional())) {
            throw new ConflitoException("Já existe um profissional com este registro.");
        }
        preencher(p, req);
        return ProfissionalDto.Response.de(p);
    }

    public void remover(Long id) {
        ProfissionalSaude p = obter(id);
        if (atendimentos.existsByProfissionalId(id) || internacoes.existsByProfissionalId(id)) {
            throw new ConflitoException("Profissional possui atendimentos ou internações registrados e não pode ser removido.");
        }
        profissionais.delete(p);
    }

    public ProfissionalSaude obter(Long id) {
        return profissionais.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    private void preencher(ProfissionalSaude p, ProfissionalDto.Request req) {
        p.setNome(req.nome());
        p.setRegistroProfissional(req.registroProfissional());
        p.setTelefone(req.telefone());
        p.setEmail(req.email());
        if (p instanceof MedicoEspecialista e) {
            if (req.especialidade() == null) throw new RegraNegocioException("A especialidade é obrigatória para médicos especialistas.");
            e.setEspecialidade(req.especialidade());
        } else if (req.especialidade() != null) {
            throw new RegraNegocioException("Somente médicos especialistas possuem especialidade.");
        }
        if (p instanceof Cirurgiao c) {
            if (req.areaCirurgica() == null || req.areaCirurgica().isBlank()) {
                throw new RegraNegocioException("A área cirúrgica é obrigatória para cirurgiões.");
            }
            c.setAreaCirurgica(req.areaCirurgica());
        } else if (req.areaCirurgica() != null) {
            throw new RegraNegocioException("Somente cirurgiões possuem área cirúrgica.");
        }
    }
}
