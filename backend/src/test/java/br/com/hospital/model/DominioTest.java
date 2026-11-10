package br.com.hospital.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import br.com.hospital.exception.ConflitoException;
import br.com.hospital.exception.RegraNegocioException;
import br.com.hospital.model.enums.Especialidade;
import br.com.hospital.model.enums.StatusConsulta;
import br.com.hospital.model.enums.StatusProcedimento;
import br.com.hospital.model.enums.StatusQuarto;
import br.com.hospital.model.enums.TipoCirurgia;
import br.com.hospital.model.enums.TipoQuarto;

class DominioTest {

    private ConsultaRotina rotina() {
        ConsultaRotina c = new ConsultaRotina();
        c.setPaciente(new Paciente());
        c.setProfissional(new MedicoClinicoGeral());
        c.setData(LocalDate.now().plusDays(1));
        c.setHorario(LocalTime.of(9, 0));
        c.setMotivo("Check-up");
        return c;
    }

    @Test
    void consultaConcluidaNaoVoltaAAgendada() {
        ConsultaRotina c = rotina();
        c.confirmar();
        c.iniciarAtendimento();
        c.concluir("ok");
        assertEquals(StatusConsulta.CONCLUIDA, c.getStatus());
        assertThrows(RegraNegocioException.class, c::confirmar);
        assertThrows(RegraNegocioException.class, c::cancelar);
    }

    @Test
    void consultaCanceladaNaoPodeSerConcluida() {
        ConsultaRotina c = rotina();
        c.cancelar();
        assertThrows(RegraNegocioException.class, () -> c.concluir("x"));
        assertThrows(RegraNegocioException.class, c::iniciarAtendimento);
    }

    @Test
    void consultaNaoPodeSerConcluidaSemTerSidoIniciada() {
        assertThrows(RegraNegocioException.class, () -> rotina().concluir("x"));
    }

    @Test
    void observacoesSoDepoisDoInicioDoAtendimento() {
        ConsultaRotina c = rotina();
        assertThrows(RegraNegocioException.class, () -> c.registrarObservacoes("x"));
        c.confirmar();
        c.iniciarAtendimento();
        assertDoesNotThrow(() -> c.registrarObservacoes("paciente estável"));
    }

    @Test
    void ocupaAgendaSomenteEnquantoAtiva() {
        ConsultaRotina c = rotina();
        assertEquals(true, c.ocupaAgenda());
        c.cancelar();
        assertEquals(false, c.ocupaAgenda());
    }

    @Test
    void consultaDeRotinaExigeClinicoGeral() {
        ConsultaRotina c = rotina();
        c.setProfissional(new Cirurgiao());
        assertThrows(RegraNegocioException.class, c::validarRealizacao);
    }

    @Test
    void consultaEspecializadaExigeEspecialidadeCompativel() {
        MedicoEspecialista cardio = new MedicoEspecialista();
        cardio.setEspecialidade(Especialidade.CARDIOLOGIA);

        ConsultaEspecializada c = new ConsultaEspecializada();
        c.setPaciente(new Paciente());
        c.setProfissional(cardio);
        c.setData(LocalDate.now().plusDays(1));
        c.setHorario(LocalTime.NOON);
        c.setMotivo("Dor no peito");

        c.setEspecialidade(Especialidade.NEUROLOGIA);
        assertThrows(RegraNegocioException.class, c::validarRealizacao);

        c.setEspecialidade(Especialidade.CARDIOLOGIA);
        assertDoesNotThrow(c::validarRealizacao);

        c.setProfissional(new MedicoClinicoGeral());
        assertThrows(RegraNegocioException.class, c::validarRealizacao);
    }

    @Test
    void procedimentoNaoPodeSerConcluidoSemSerIniciado() {
        ProcedimentoCirurgico p = new ProcedimentoCirurgico();
        assertThrows(RegraNegocioException.class, () -> p.concluir("ok"));
        p.iniciar();
        assertEquals(StatusProcedimento.EM_ANDAMENTO, p.getStatus());
        p.concluir("recuperação sem intercorrências");
        assertEquals(StatusProcedimento.CONCLUIDO, p.getStatus());
        assertThrows(RegraNegocioException.class, p::cancelar);
    }

    @Test
    void procedimentoExigeCirurgiao() {
        ProcedimentoCirurgico p = new ProcedimentoCirurgico();
        p.setPaciente(new Paciente());
        p.setProfissional(new MedicoClinicoGeral());
        p.setData(LocalDate.now().plusDays(2));
        p.setHorario(LocalTime.of(7, 30));
        p.setDescricao("Apendicectomia");
        p.setTipoCirurgia(TipoCirurgia.ELETIVA);
        assertThrows(RegraNegocioException.class, p::validarRealizacao);

        p.setProfissional(new Cirurgiao());
        assertDoesNotThrow(p::validarRealizacao);
    }

    @Test
    void quartoNaoUltrapassaCapacidade() {
        Quarto q = new Quarto();
        q.setNumero(101);
        q.setTipo(TipoQuarto.APARTAMENTO);
        q.setCapacidadeMaxima(2);
        q.ocuparVaga();
        assertEquals(StatusQuarto.DISPONIVEL, q.getStatus());
        q.ocuparVaga();
        assertEquals(StatusQuarto.OCUPADO, q.getStatus());
        assertThrows(ConflitoException.class, q::ocuparVaga);
        q.liberarVaga();
        assertEquals(StatusQuarto.DISPONIVEL, q.getStatus());
    }

    @Test
    void capacidadeRespeitaLimiteDoTipo() {
        Quarto q = new Quarto();
        q.setTipo(TipoQuarto.UTI);
        q.setCapacidadeMaxima(3);
        assertThrows(RegraNegocioException.class, q::validarCapacidade);
        q.setCapacidadeMaxima(1);
        assertDoesNotThrow(q::validarCapacidade);
    }

    @Test
    void altaNaoPodeSerAnteriorAEntradaNemRepetida() {
        Internacao i = new Internacao();
        i.setDataEntrada(LocalDate.of(2026, 3, 10));
        assertThrows(RegraNegocioException.class, () -> i.registrarAlta(LocalDate.of(2026, 3, 9)));
        i.registrarAlta(LocalDate.of(2026, 3, 12));
        assertThrows(RegraNegocioException.class, () -> i.registrarAlta(LocalDate.of(2026, 3, 13)));
    }
}
