package br.com.fernandoramosjr.sisgerdoc.service;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import br.com.fernandoramosjr.sisgerdoc.model.dto.DocumentoDownloadDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public interface DocumentoService {
    Documento criarNovoDocumento(String name, MultipartFile file) throws IOException;
    void atualizarDocumento(Long id, String nome, MultipartFile file) throws IOException;
    void deletarDocumento(Long id) throws IOException;
    DocumentoDownloadDto buscarDocumentoParaDownload(Long id) throws MalformedURLException, FileNotFoundException;
}
