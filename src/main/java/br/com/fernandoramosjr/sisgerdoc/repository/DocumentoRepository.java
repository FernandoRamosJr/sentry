package br.com.fernandoramosjr.sisgerdoc.repository;

import br.com.fernandoramosjr.sisgerdoc.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {}