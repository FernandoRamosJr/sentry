package br.com.fernandoramosjr.sisgerdoc.service;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import br.com.fernandoramosjr.sisgerdoc.model.dto.DocumentoDownloadDto;
import br.com.fernandoramosjr.sisgerdoc.repository.DocumentoRepository;
import br.com.fernandoramosjr.sisgerdoc.util.DocumentoUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;
    private static final Logger logger = LogManager.getLogger(DocumentoServiceImpl.class);

    @Value("${file.upload-dir}")
    String uploadDir;

    public DocumentoServiceImpl(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    @Override
    public Documento criarNovoDocumento(String nome, MultipartFile file) throws IOException {
        logger.info("Criando documento. Nome: {}", nome);
        if (file.isEmpty()) {
            logger.error("Tentativa de criação com arquivo vazio. Nome: {}", nome);
            throw new IOException("Arquivo enviado está vazio.");
        }

        Documento documento = new Documento(nome, LocalDateTime.now());
        documento = documentoRepository.save(documento);
        logger.debug("Documento salvo para geração do ID. ID: {}", documento.getId());

        DocumentoUtil fileManager = new DocumentoUtil(uploadDir);
        String fileName = fileManager.geraFileName(nome, documento.getId(), file);
        Path filePath = fileManager.getFilePath(fileName);
        fileManager.copiaConteudo(file, filePath);

        documento.setFileName(fileName);
        documento.setFilePath(filePath.toString());
        documento.setFileSize(file.getSize());

        documento = documentoRepository.save(documento);
        logger.info("Documento criado com sucesso. ID: {} - fileName: {}", documento.getId(), fileName);
        return documento;
    }

    @Override
    public DocumentoDownloadDto buscarDocumentoParaDownload(Long id) throws MalformedURLException, FileNotFoundException {
        logger.info("Preparando download do documento com ID: {}", id);
        Optional<Documento> documento = documentoRepository.findById(id);
        if (documento.isPresent()) {
            Documento documentoDownload = documento.get();
            Path filePath = Paths.get(documentoDownload.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.error("Arquivo não encontrado ou não legível. ID: {}", id);
                throw new FileNotFoundException("Arquivo não encontrado ou inelegível");
            }

            String fileName = StringUtils.cleanPath(documentoDownload.getFileName());
            logger.info("Download preparado para o documento. ID: {} - fileName: {}", id, fileName);
            return new DocumentoDownloadDto(resource, fileName);
        }
        logger.error("Documento não encontrado para download. ID: {}", id);
        throw new FileNotFoundException("Documento não encontrado");
    }

    @Override
    public void atualizarDocumento(Long id, String nome, MultipartFile file) throws IOException {
        logger.info("Atualizando documento com ID: {}", id);
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Documento não encontrado para atualização. ID: {}", id);
                    return new RuntimeException("Documento não encontrado");
                });
        if (nome != null && !nome.isEmpty()) {
            documento.setNome(nome);
            logger.debug("Nome do documento atualizado para: {}", nome);
        }

        if (file != null && !file.isEmpty()) {
            if (documento.getFilePath() != null) {
                Path antigoFilePath = Paths.get(documento.getFilePath());
                if (Files.exists(antigoFilePath)) {
                    Files.delete(antigoFilePath);
                    logger.debug("Arquivo antigo deletado: {}", documento.getFilePath());
                }
            }

            DocumentoUtil documentoUtil = new DocumentoUtil(uploadDir);
            String fileName = documentoUtil.geraFileName(documento.getNome(), documento.getId(), file);
            Path newFilePath = documentoUtil.getFilePath(fileName);
            documentoUtil.copiaConteudo(file, newFilePath);

            documento.setFileName(fileName);
            documento.setFilePath(newFilePath.toString());
            documento.setFileSize(file.getSize());
            logger.info("Arquivo atualizado para o documento ID: {} - Novo fileName: {}", documento.getId(), fileName);
        }
        documentoRepository.save(documento);
        logger.info("Documento atualizado com sucesso. ID: {}", id);
    }

    @Override
    public void deletarDocumento(Long id) throws IOException {
        logger.info("Excluindo documento com ID: {}", id);
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Documento não encontrado para exclusão. ID: {}", id);
                    return new RuntimeException("Documento não encontrado");
                });
        Path filePath = Paths.get(documento.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.debug("Arquivo deletado: {}", documento.getFilePath());
        }
        documentoRepository.delete(documento);
        logger.info("Documento excluído com sucesso. ID: {}", id);
    }
}

