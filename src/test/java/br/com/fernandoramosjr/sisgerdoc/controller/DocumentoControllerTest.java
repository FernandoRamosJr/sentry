package br.com.fernandoramosjr.sisgerdoc.controller;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import br.com.fernandoramosjr.sisgerdoc.model.dto.DocumentoDownloadDto;
import br.com.fernandoramosjr.sisgerdoc.service.DocumentoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(DocumentoControllerTest.MocksConfig.class)
public class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentoService documentoService;

    @TestConfiguration
    static class MocksConfig {
        @Bean
        public DocumentoService documentoService() {
            return Mockito.mock(DocumentoService.class);
        }
    }

    @Test
    public void criarDocumentoComSucessoTest() throws Exception {
        String nome = "teste";
        MockMultipartFile file = new MockMultipartFile("file", "teste.txt", MediaType.TEXT_PLAIN_VALUE, "Conteúdo".getBytes());

        Documento documento = new Documento();
        documento.setId(1L);
        Mockito.when(documentoService.criarNovoDocumento(eq(nome), any())).thenReturn(documento);

        mockMvc.perform(multipart("/documentos")
                        .file(file)
                        .param("nome", nome))
                .andExpect(status().isOk())
                .andExpect(content().string("Documento criado com ID: " + documento.getId()));
    }

    @Test
    public void criarDocumentoComIOExceptionTest() throws Exception {
        String nome = "teste";
        MockMultipartFile file = new MockMultipartFile("file", "teste.txt", MediaType.TEXT_PLAIN_VALUE, "Conteúdo".getBytes());
        Mockito.when(documentoService.criarNovoDocumento(eq(nome), any()))
                .thenThrow(new IOException("Erro ao salvar"));

        mockMvc.perform(multipart("/documentos")
                        .file(file)
                        .param("nome", nome))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao salvar o documento"));
    }

    @Test
    public void buscarDocumentoComSucessoTest() throws Exception {
        Long id = 1L;
        DocumentoDownloadDto dto = new DocumentoDownloadDto();
        dto.setFileName("teste.txt");
        byte[] conteudo = "Conteúdo".getBytes();
        Resource resource = new ByteArrayResource(conteudo);
        dto.setResource(resource);
        Mockito.when(documentoService.buscarDocumentoParaDownload(id)).thenReturn(dto);

        mockMvc.perform(get("/documentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFileName() + "\""))
                .andExpect(content().bytes(conteudo));
    }

    @Test
    public void buscarDocumentoNaoEncontradoTest() throws Exception {
        Long id = 1L;
        Mockito.when(documentoService.buscarDocumentoParaDownload(id))
                .thenThrow(new FileNotFoundException("Documento não encontrado"));

        mockMvc.perform(get("/documentos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletarDocumentoComSucessoTest() throws Exception {
        Long id = 1L;
        Mockito.doNothing().when(documentoService).deletarDocumento(id);

        mockMvc.perform(delete("/documentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Documento apagado com sucesso"));
    }

    @Test
    public void deletarDocumentoComIOExceptionTest() throws Exception {
        Long id = 1L;
        Mockito.doThrow(new IOException("Erro")).when(documentoService).deletarDocumento(id);

        mockMvc.perform(delete("/documentos/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao apagar o documento"));
    }

    @Test
    public void deletarDocumentoNaoEncontradoTest() throws Exception {
        Long id = 1L;
        Mockito.doThrow(new RuntimeException("Documento não encontrado")).when(documentoService).deletarDocumento(id);

        mockMvc.perform(delete("/documentos/{id}", id))
                .andExpect(status().isNotFound());
    }
}
