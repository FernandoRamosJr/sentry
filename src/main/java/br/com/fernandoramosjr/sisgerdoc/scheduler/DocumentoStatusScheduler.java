package br.com.fernandoramosjr.sisgerdoc.scheduler;

import br.com.fernandoramosjr.sisgerdoc.repository.DocumentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocumentoStatusScheduler {
    private static final Logger logger = LoggerFactory.getLogger(DocumentoStatusScheduler.class);
    private final DocumentoRepository documentoRepository;

    public DocumentoStatusScheduler(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    @Async
    @Scheduled(cron = "0 30 20 * * ?")
    public void logDocumentoStatus() {
        long count = documentoRepository.count();
        long totalBytes = documentoRepository.findAll()
                .stream()
                .mapToLong(doc -> doc.getFileSize() != null ? doc.getFileSize() : 0L)
                .sum();
        logger.info("Número de arquivos: {}. Total armazenado em bytes: {}", count, totalBytes);
    }
}
