package br.com.fernandoramosjr.sisgerdoc.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;

public class DocumentoUtil {
    private final String uploadDir;

    public DocumentoUtil(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Path verificaPath() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    public String getExtensao(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName.lastIndexOf('.') != -1) {
            return originalFileName.substring(originalFileName.lastIndexOf('.'));
        }
        return "";
    }

    public String normalizaNome(String nome) {
        return Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll(" ", "_");
    }

    public String geraFileName(String nome, Long id, MultipartFile file) {
        String normalizedName = normalizaNome(nome);
        String extension = getExtensao(file);
        return normalizedName + "_" + String.format("%02d", id) + extension;
    }

    public Path getFilePath(String fileName) throws IOException {
        return verificaPath().resolve(fileName);
    }

    public void copiaConteudo(MultipartFile file, Path targetPath) throws IOException {
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
