package br.com.fernandoramosjr.sisgerdoc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String filePath;
    private Long fileSize;
    private String fileName;
    private LocalDateTime criadoEm;

    public Documento() {}

    public Documento(Long id, String nome, String filePath, Long fileSize, String fileName, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.criadoEm = criadoEm;
    }

    public Documento(String nome, LocalDateTime criadoEm) {
        this.nome = nome;
        this.criadoEm = criadoEm;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
