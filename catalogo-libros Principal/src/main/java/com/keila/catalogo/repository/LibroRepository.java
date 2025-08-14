
package com.keila.catalogo.repository;

import com.keila.catalogo.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByIdiomaIgnoreCase(String idioma);
    long countByIdiomaIgnoreCase(String idioma);
    Optional<Libro> findByTituloIgnoreCase(String titulo);
}
