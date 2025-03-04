package br.com.fernandoramosjr.sisgerdoc.service;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import br.com.fernandoramosjr.sisgerdoc.model.dto.DocumentoDownloadDto;
import br.com.fernandoramosjr.sisgerdoc.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DocumentoServiceImplTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private DocumentoServiceImpl documentoService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(documentoService, "uploadDir", tempDir.toString());
    }

    @Test
    public void criarDocumentoComSucessoTest() throws IOException {
        String nome = "teste";
        byte[] content = "Conteúdo".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "teste.txt", "text/plain", content);

        Mockito.when(documentoRepository.save(Mockito.any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            if (doc.getId() == null) {
                doc.setId(1L);
            }
            return doc;
        });

        Documento documento = documentoService.criarNovoDocumento(nome, file);

        assertNotNull(documento.getId());
        assertEquals(nome, documento.getNome());
        assertNotNull(documento.getFileName());
        assertNotNull(documento.getFilePath());
        assertTrue(documento.getFileSize() > 0);

        Path filePath = Path.of(documento.getFilePath());
        assertTrue(Files.exists(filePath));
        byte[] fileContent = Files.readAllBytes(filePath);
        assertArrayEquals(content, fileContent);
    }

    @Test
    public void criarDocumentoComArquivoVazioTest() {
        String nome = "teste";
        MockMultipartFile file = new MockMultipartFile("file", "teste.txt", "text/plain", new byte[0]);

        IOException exception = assertThrows(IOException.class, () -> {
            documentoService.criarNovoDocumento(nome, file);
        });
        assertEquals("Arquivo enviado está vazio.", exception.getMessage());
    }

    @Test
    public void buscarDocumentoParaDownloadComSucessoTest() throws IOException {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNome("teste.txt");
        String fileName = "teste_1.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, "Conteúdo".getBytes());
        documento.setFileName(fileName);
        documento.setFilePath(filePath.toString());
        documento.setFileSize(Files.size(filePath));

        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        DocumentoDownloadDto dto = documentoService.buscarDocumentoParaDownload(1L);

        assertNotNull(dto);
        assertEquals("teste_1.txt", dto.getFileName());
        assertNotNull(dto.getResource());
        assertTrue(dto.getResource().exists());
    }

    @Test
    public void buscarDocumentoParaDownloadComArquivoNaoEncontradoTest() {
        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.empty());
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            documentoService.buscarDocumentoParaDownload(1L);
        });
        assertEquals("Documento não encontrado", exception.getMessage());
    }

    @Test
    public void buscarDocumentoParaDownloadComArquivoIlegivelTest() {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNome("teste.txt");
        documento.setFileName("teste_1.txt");
        documento.setFilePath(tempDir.resolve("non_existing.txt").toString());
        documento.setFileSize(0L);

        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            documentoService.buscarDocumentoParaDownload(1L);
        });
        assertEquals("Arquivo não encontrado ou inelegível", exception.getMessage());
    }

    @Test
    public void atualizarDocumentoComSucessoApenasComNomeTest() throws IOException {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNome("oldName");
        documento.setFilePath(tempDir.resolve("old.txt").toString());
        Files.write(Path.of(documento.getFilePath()), "Old Content".getBytes());

        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        Mockito.when(documentoRepository.save(Mockito.any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        documentoService.atualizarDocumento(1L, "newName", null);

        assertEquals("newName", documento.getNome());
        // Verifica que o arquivo antigo continua existente
        assertTrue(Files.exists(Path.of(documento.getFilePath())));
    }

    @Test
    public void atualizarDocumentoComSucessoComNomeEArquivoTest() throws IOException {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNome("oldName");
        documento.setFilePath(tempDir.resolve("old.txt").toString());
        Files.write(Path.of(documento.getFilePath()), "Old Content".getBytes());

        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        Mockito.when(documentoRepository.save(Mockito.any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newContent = "New Content";
        MockMultipartFile newFile = new MockMultipartFile("file", "new.txt", "text/plain", newContent.getBytes());

        documentoService.atualizarDocumento(1L, "newName", newFile);

        assertFalse(Files.exists(tempDir.resolve("old.txt")));

        assertEquals("newName", documento.getNome());
        assertNotNull(documento.getFileName());
        assertNotNull(documento.getFilePath());
        Path newFilePath = Path.of(documento.getFilePath());
        assertTrue(Files.exists(newFilePath));
        byte[] updatedContent = Files.readAllBytes(newFilePath);
        assertArrayEquals(newContent.getBytes(), updatedContent);
    }

    @Test
    public void atualizarDocumentoNaoEncontradoTest() {
        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentoService.atualizarDocumento(1L, "newName", null);
        });
        assertEquals("Documento não encontrado", exception.getMessage());
    }

    @Test
    public void deletarDocumentoComSucessoTest() throws IOException {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNome("teste");
        String fileName = "teste_1.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, "Content".getBytes());
        documento.setFileName(fileName);
        documento.setFilePath(filePath.toString());
        documento.setFileSize(Files.size(filePath));

        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        Mockito.doNothing().when(documentoRepository).delete(documento);

        documentoService.deletarDocumento(1L);

        assertFalse(Files.exists(filePath));
        Mockito.verify(documentoRepository).delete(documento);
    }

    @Test
    public void deletarDocumentoNaoEncontradoTest() {
        Mockito.when(documentoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentoService.deletarDocumento(1L);
        });
        assertEquals("Documento não encontrado", exception.getMessage());
    }
}
