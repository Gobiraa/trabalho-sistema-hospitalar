package br.com.hospital.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hospital.dto.QuartoDto;
import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RecursoNaoEncontradoException;
import br.com.hospital.model.Quarto;
import br.com.hospital.model.enums.StatusQuarto;
import br.com.hospital.model.enums.TipoQuarto;
import br.com.hospital.repository.InternacaoRepository;
import br.com.hospital.repository.QuartoRepository;

@Service
@Transactional
public class QuartoService {

    private final QuartoRepository quartos;
    private final InternacaoRepository internacoes;

    public QuartoService(QuartoRepository quartos, InternacaoRepository internacoes) {
        this.quartos = quartos;
        this.internacoes = internacoes;
    }

    public QuartoDto.Response criar(QuartoDto.Request req) {
        if (quartos.existsByNumero(req.numero())) throw new ConflitoException("Já existe um quarto com o número " + req.numero() + ".");
        Quarto q = new Quarto();
        preencher(q, req);
        return QuartoDto.Response.de(quartos.save(q));
    }

    @Transactional(readOnly = true)
    public List<QuartoDto.Response> listar(StatusQuarto status, TipoQuarto tipo) {
        return quartos.findAll().stream()
                .filter(q -> status == null || q.getStatus() == status)
                .filter(q -> tipo == null || q.getTipo() == tipo)
                .sorted(Comparator.comparingInt(Quarto::getNumero))
                .map(QuartoDto.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuartoDto.Response buscar(Long id) {
        return QuartoDto.Response.de(obter(id));
    }

    public QuartoDto.Response atualizar(Long id, QuartoDto.Request req) {
        Quarto q = obter(id);
        if (req.numero() != q.getNumero() && quartos.existsByNumero(req.numero())) {
            throw new ConflitoException("Já existe um quarto com o número " + req.numero() + ".");
        }
        preencher(q, req);
        return QuartoDto.Response.de(q);
    }

    public void remover(Long id) {
        Quarto q = obter(id);
        if (internacoes.existsByQuartoId(id)) {
            throw new ConflitoException("Quarto possui internações registradas e não pode ser removido.");
        }
        quartos.delete(q);
    }

    public Quarto obter(Long id) {
        return quartos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", id));
    }

    private void preencher(Quarto q, QuartoDto.Request req) {
        q.setNumero(req.numero());
        q.setAndar(req.andar());
        q.setTipo(req.tipo());
        q.setCapacidadeMaxima(req.capacidadeMaxima());
        q.validarCapacidade();
    }
}
