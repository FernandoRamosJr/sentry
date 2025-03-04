package br.com.fernandoramosjr.sisgerdoc.controller;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import br.com.fernandoramosjr.sisgerdoc.model.dto.DocumentoDownloadDto;
import br.com.fernandoramosjr.sisgerdoc.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {

    private static final Logger logger = LogManager.getLogger(DocumentoController.class);
    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @Operation(
            summary = "Cria um novo documento",
            description = "Recebe o nome do documento e um arquivo binário. Insere um registro no banco de dados, armazena o arquivo no sistema de arquivos e retorna o ID do documento.",
            security = @SecurityRequirement(name = "ApiKeyAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento criado com sucesso",
                            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Erro ao salvar o documento",
                            content = @Content(mediaType = "text/plain"))
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> criarNovoDocumento(
            @Parameter(description = "Nome do documento", required = true, example = "Contrato")
            @RequestParam("nome") String nome,
            @Parameter(description = "Arquivo binário do documento", required = true)
            @RequestParam("file") MultipartFile file) {
        logger.info("Iniciando criação do documento. Nome: {}", nome);
        try {
            Documento documento = documentoService.criarNovoDocumento(nome, file);
            logger.info("Documento criado com sucesso. ID: {}", documento.getId());
            return ResponseEntity.ok("Documento criado com ID: " + documento.getId());
        } catch (IOException e) {
            logger.error("Erro ao salvar o documento. Nome: {}. Erro: {}", nome, e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao salvar o documento");
        }
    }

    @Operation(
            summary = "Retorna o documento pelo ID e inicia o download",
            description = "Busca o documento no banco de dados e retorna o arquivo para download. Se o documento não for encontrado ou ocorrer erro na leitura, retorna 404.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Download iniciado com sucesso",
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Documento não encontrado ou erro no download",
                            content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Resource> buscarDocumento(
            @Parameter(description = "ID do documento a ser baixado", required = true, example = "1")
            @PathVariable Long id) {
        logger.info("Iniciando download do documento com ID: {}", id);
        try {
            DocumentoDownloadDto download = documentoService.buscarDocumentoParaDownload(id);
            String contentDisposition = "attachment; filename=\"" + download.getFileName() + "\"";
            logger.info("Documento encontrado. Iniciando envio do arquivo: {}", download.getFileName());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(download.getResource());
        } catch (FileNotFoundException | MalformedURLException e) {
            logger.error("Documento não encontrado ou erro no download. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Atualiza o documento existente",
            description = "Atualiza o nome e/ou o arquivo binário do documento existente. Se o documento não existir, retorna 404.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso",
                            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Documento não encontrado para atualização",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Erro ao atualizar o documento",
                            content = @Content(mediaType = "text/plain"))
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> atualizarDocumento(
            @Parameter(description = "ID do documento a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Novo nome do documento (opcional)", required = false, example = "NovoContrato")
            @RequestParam(value = "nome", required = false) String nome,
            @Parameter(description = "Novo arquivo binário (opcional)", required = false)
            @RequestParam(value = "file", required = false) MultipartFile file) {
        logger.info("Iniciando atualização do documento com ID: {}", id);
        try {
            documentoService.atualizarDocumento(id, nome, file);
            logger.info("Documento atualizado com sucesso. ID: {}", id);
            return ResponseEntity.ok("Documento atualizado com sucesso");
        } catch (IOException e) {
            logger.error("Erro ao atualizar o documento. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar o documento");
        } catch (RuntimeException ex) {
            logger.error("Documento não encontrado para atualização. ID: {}. Erro: {}", id, ex.getMessage(), ex);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Apaga o documento pelo ID",
            description = "Remove o documento do banco de dados e exclui o arquivo do sistema de arquivos. Se o documento não existir, retorna 404.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento apagado com sucesso",
                            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Documento não encontrado para exclusão",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Erro ao apagar o documento",
                            content = @Content(mediaType = "text/plain"))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarDocumento(
            @Parameter(description = "ID do documento a ser excluído", required = true, example = "1")
            @PathVariable Long id) {
        logger.info("Iniciando exclusão do documento com ID: {}", id);
        try {
            documentoService.deletarDocumento(id);
            logger.info("Documento apagado com sucesso. ID: {}", id);
            return ResponseEntity.ok("Documento apagado com sucesso");
        } catch (IOException e) {
            logger.error("Erro ao apagar o documento. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao apagar o documento");
        } catch (RuntimeException ex) {
            logger.error("Documento não encontrado para exclusão. ID: {}. Erro: {}", id, ex.getMessage(), ex);
            return ResponseEntity.notFound().build();
        }
    }
}