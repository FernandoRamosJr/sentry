package br.com.fernandoramosjr.sisgerdoc.model.dto;

import org.springframework.core.io.Resource;

public class DocumentoDownloadDto {
    private Resource resource;
    private String fileName;

    public DocumentoDownloadDto() {}

    public DocumentoDownloadDto(Resource resource, String fileName) {
        this.resource = resource;
        this.fileName = fileName;
    }

    public Resource getResource() {
        return resource;
    }
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}