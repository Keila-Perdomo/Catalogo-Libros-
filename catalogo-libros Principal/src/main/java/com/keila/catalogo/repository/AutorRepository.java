
package com.keila.catalogo.repository;

import com.keila.catalogo.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    List<Autor> findByAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(Integer inicio, Integer fin);
    Optional<Autor> findByNombreIgnoreCase(String nombre);
}
