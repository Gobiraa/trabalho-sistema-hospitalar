package br.com.hospital;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static final AtomicInteger SEQ = new AtomicInteger(1000);
    private static final String[] CPFS = {"52998224725", "11144477735", "39053344705", "16899535009", "71428793860"};

    private long criar(String url, String corpo) throws Exception {
        String resp = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode n = json.readTree(resp);
        return n.get("id").asLong();
    }

    private ResultActions postar(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private long paciente(String cpf) throws Exception {
        return criar("/pacientes", """
                {"nome":"Maria","cpf":"%s","dataNascimento":"1990-05-01","telefone":"3199999","endereco":"Rua A, 1","email":"m@x.com"}
                """.formatted(cpf));
    }

    private long profissional(String categoria, String extra) throws Exception {
        return criar("/profissionais", """
                {"categoria":"%s","nome":"Dr %d","registroProfissional":"REG%d","telefone":"3188","email":"d@x.com"%s}
                """.formatted(categoria, SEQ.get(), SEQ.incrementAndGet(), extra));
    }

    private String amanha() { return LocalDate.now().plusDays(1).toString(); }

    @Test
    void cpfInvalidoERejeitadoEDuplicadoDaConflito() throws Exception {
        postar("/pacientes", """
                {"nome":"X","cpf":"11111111111","dataNascimento":"1990-05-01","telefone":"1","endereco":"r","email":"a@b.com"}
                """).andExpect(status().isUnprocessableEntity());
        paciente(CPFS[0]);
        postar("/pacientes", """
                {"nome":"Y","cpf":"529.982.247-25","dataNascimento":"1990-05-01","telefone":"1","endereco":"r","email":"a@b.com"}
                """).andExpect(status().isConflict());
    }

    @Test
    void validacaoDeCamposRetorna400() throws Exception {
        postar("/pacientes", "{}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void profissionalNaoTemDoisAtendimentosNoMesmoHorario() throws Exception {
        long pac = paciente(CPFS[1]);
        long clinico = profissional("CLINICO_GERAL", "");
        String corpo = """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"10:00","motivo":"Rotina"}
                """.formatted(pac, clinico, amanha());
        postar("/consultas", corpo).andExpect(status().isCreated()).andExpect(jsonPath("$.status", is("AGENDADA")));
        postar("/consultas", corpo).andExpect(status().isConflict());

        mvc.perform(get("/profissionais/" + clinico + "/disponibilidade?data=" + amanha() + "&horario=10:00"))
                .andExpect(jsonPath("$.disponivel", is(false)));
    }

    @Test
    void fluxoDeStatusDaConsultaEImpedeTransicoesInvalidas() throws Exception {
        long pac = paciente(CPFS[2]);
        long clinico = profissional("CLINICO_GERAL", "");
        long id = criar("/consultas", """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"11:00","motivo":"Rotina"}
                """.formatted(pac, clinico, amanha()));

        postar("/consultas/" + id + "/concluir", "{}").andExpect(status().isUnprocessableEntity());
        postar("/consultas/" + id + "/confirmar", "").andExpect(jsonPath("$.status", is("CONFIRMADA")));
        postar("/consultas/" + id + "/iniciar", "").andExpect(jsonPath("$.status", is("EM_ATENDIMENTO")));
        postar("/consultas/" + id + "/concluir", "{\"observacoes\":\"tudo bem\"}")
                .andExpect(jsonPath("$.status", is("CONCLUIDA")));
        postar("/consultas/" + id + "/confirmar", "").andExpect(status().isUnprocessableEntity());
        postar("/consultas/" + id + "/cancelar", "").andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/pacientes/" + pac + "/historico"))
                .andExpect(jsonPath("$.consultas", hasSize(1)))
                .andExpect(jsonPath("$.informacoesRelevantes[0]").exists());
    }

    @Test
    void consultaCanceladaLiberaOHorario() throws Exception {
        long pac = paciente(CPFS[3]);
        long clinico = profissional("CLINICO_GERAL", "");
        String corpo = """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"12:00","motivo":"Rotina"}
                """.formatted(pac, clinico, amanha());
        long id = criar("/consultas", corpo);
        postar("/consultas/" + id + "/cancelar", "").andExpect(jsonPath("$.status", is("CANCELADA")));
        postar("/consultas", corpo).andExpect(status().isCreated());
    }

    @Test
    void consultaEspecializadaExigeEspecialistaCompativel() throws Exception {
        long pac = paciente(CPFS[4]);
        long cardio = profissional("ESPECIALISTA", ",\"especialidade\":\"CARDIOLOGIA\"");
        long clinico = profissional("CLINICO_GERAL", "");

        postar("/consultas", """
                {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"08:00","motivo":"x","especialidade":"NEUROLOGIA"}
                """.formatted(pac, cardio, amanha())).andExpect(status().isUnprocessableEntity());
        postar("/consultas", """
                {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"08:00","motivo":"x","especialidade":"CARDIOLOGIA"}
                """.formatted(pac, clinico, amanha())).andExpect(status().isUnprocessableEntity());
        postar("/consultas", """
                {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"08:00","motivo":"x","especialidade":"CARDIOLOGIA"}
                """.formatted(pac, cardio, amanha())).andExpect(status().isCreated());

        mvc.perform(get("/profissionais?especialidade=CARDIOLOGIA&data=" + amanha() + "&horario=08:00"))
                .andExpect(jsonPath("$[?(@.id==" + cardio + ")]", hasSize(0)));
    }

    @Test
    void internacaoRespeitaCapacidadeDoQuarto() throws Exception {
        long medico = profissional("CLINICO_GERAL", "");
        long quarto = criar("/quartos", "{\"numero\":" + SEQ.incrementAndGet() + ",\"andar\":1,\"capacidadeMaxima\":1,\"tipo\":\"UTI\"}");
        long p1 = paciente("98765432100");
        long p2 = paciente("12345678909");

        String hoje = LocalDate.now().toString();
        long internacao = criar("/internacoes", """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,"dataEntrada":"%s"}
                """.formatted(p1, medico, quarto, hoje));
        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.status", is("OCUPADO")));

        postar("/internacoes", """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,"dataEntrada":"%s"}
                """.formatted(p2, medico, quarto, hoje)).andExpect(status().isConflict());
        // mesmo paciente não pode ter duas internações ativas
        postar("/internacoes", """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,"dataEntrada":"%s"}
                """.formatted(p1, medico, quarto, hoje)).andExpect(status().isConflict());

        postar("/internacoes/" + internacao + "/alta", "").andExpect(jsonPath("$.ativa", is(false)));
        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.status", is("DISPONIVEL")));
        postar("/internacoes", """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,"dataEntrada":"%s"}
                """.formatted(p2, medico, quarto, hoje)).andExpect(status().isCreated());
    }

    @Test
    void procedimentoCirurgicoSoConcluiSeIniciado() throws Exception {
        long pac = paciente("45317828791");
        long cirurgiao = profissional("CIRURGIAO", ",\"areaCirurgica\":\"Geral\"");
        long clinico = profissional("CLINICO_GERAL", "");

        String corpo = """
                {"pacienteId":%d,"cirurgiaoId":%d,"data":"%s","horario":"07:00","descricao":"Apendicectomia","tipoCirurgia":"ELETIVA"}
                """;
        postar("/procedimentos-cirurgicos", corpo.formatted(pac, clinico, amanha())).andExpect(status().isUnprocessableEntity());
        long id = criar("/procedimentos-cirurgicos", corpo.formatted(pac, cirurgiao, amanha()));

        postar("/procedimentos-cirurgicos/" + id + "/concluir", "{}").andExpect(status().isUnprocessableEntity());
        postar("/procedimentos-cirurgicos/" + id + "/iniciar", "").andExpect(jsonPath("$.status", is("EM_ANDAMENTO")));
        postar("/procedimentos-cirurgicos/" + id + "/concluir", "{\"observacoesPosOperatorias\":\"ok\"}")
                .andExpect(jsonPath("$.status", is("CONCLUIDO")));

        mvc.perform(get("/profissionais/" + cirurgiao + "/procedimentos")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filaDeEmergenciaOrdenadaPorPrioridade() throws Exception {
        long pac = paciente("30997352051");
        long medico = profissional("CLINICO_GERAL", "");
        criar("/emergencias", "{\"pacienteId\":" + pac + ",\"prioridade\":\"BAIXA\",\"queixa\":\"dor leve\"}");
        long critico = criar("/emergencias", "{\"pacienteId\":" + pac + ",\"prioridade\":\"CRITICA\",\"queixa\":\"parada\"}");

        mvc.perform(get("/emergencias?status=AGUARDANDO_AVALIACAO"))
                .andExpect(jsonPath("$[0].prioridade", is("CRITICA")));
        postar("/emergencias/" + critico + "/concluir", "{}").andExpect(status().isUnprocessableEntity());
        postar("/emergencias/" + critico + "/iniciar", "{\"profissionalId\":" + medico + "}")
                .andExpect(jsonPath("$.status", is("EM_ATENDIMENTO")));
        postar("/emergencias/" + critico + "/concluir", "{}").andExpect(jsonPath("$.status", is("CONCLUIDO")));
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pacientes/999999")).andExpect(status().isNotFound());
    }
}
