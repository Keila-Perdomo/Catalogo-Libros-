
package com.keila.catalogo.service;

import com.google.gson.*;
import com.keila.catalogo.model.Autor;
import com.keila.catalogo.model.Libro;
import com.keila.catalogo.repository.AutorRepository;
import com.keila.catalogo.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public LibroService(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void buscarPorTitulo(String titulo) throws IOException {
        // Evitar duplicados por título (case-insensitive)
        Optional<Libro> existente = libroRepository.findByTituloIgnoreCase(titulo);
        if (existente.isPresent()) {
            System.out.println("⚠ El libro \"" + existente.get().getTitulo() + "\" ya está registrado.");
            return;
        }

        String url = "https://gutendex.com/books/?search=" + titulo.replace(" ", "%20");
        String json;
        try (InputStream in = new URL(url).openStream()) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.getAsJsonArray("results");

        if (results == null || results.size() == 0) {
            System.out.println("❌ No se encontró el libro con ese título.");
            return;
        }

        JsonObject libroJson = results.get(0).getAsJsonObject();

        String tituloReal = libroJson.has("title") && !libroJson.get("title").isJsonNull()
                ? libroJson.get("title").getAsString() : titulo;

        // Checar duplicados por el título real
        if (libroRepository.findByTituloIgnoreCase(tituloReal).isPresent()) {
            System.out.println("⚠ El libro \"" + tituloReal + "\" ya está registrado.");
            return;
        }

        String idioma = "desconocido";
        if (libroJson.has("languages") && libroJson.get("languages").isJsonArray()
                && libroJson.getAsJsonArray("languages").size() > 0) {
            idioma = libroJson.getAsJsonArray("languages").get(0).getAsString();
        }

        Integer descargas = null;
        if (libroJson.has("download_count") && !libroJson.get("download_count").isJsonNull()) {
            descargas = libroJson.get("download_count").getAsInt();
        }

        Autor autor = null;
        if (libroJson.has("authors") && libroJson.get("authors").isJsonArray()
                && libroJson.getAsJsonArray("authors").size() > 0) {
            JsonObject autorJson = libroJson.getAsJsonArray("authors").get(0).getAsJsonObject();
            String nombreAutor = autorJson.has("name") && !autorJson.get("name").isJsonNull()
                    ? autorJson.get("name").getAsString() : "Autor desconocido";
            Integer nacimiento = autorJson.has("birth_year") && !autorJson.get("birth_year").isJsonNull()
                    ? autorJson.get("birth_year").getAsInt() : null;
            Integer muerte = autorJson.has("death_year") && !autorJson.get("death_year").isJsonNull()
                    ? autorJson.get("death_year").getAsInt() : null;

            autor = autorRepository.findByNombreIgnoreCase(nombreAutor).orElseGet(() -> {
                Autor nuevo = new Autor(nombreAutor, nacimiento, muerte);
                return autorRepository.save(nuevo);
            });
        }

        Libro libro = new Libro(tituloReal, idioma, descargas, autor);
        try {
            libroRepository.save(libro);
            System.out.println("✅ Libro guardado: " + tituloReal);
        } catch (Exception e) {
            System.out.println("❌ No se pudo guardar el libro: " + e.getMessage());
        }
    }
}
