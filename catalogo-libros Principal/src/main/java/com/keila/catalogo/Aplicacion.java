
package com.keila.catalogo;

import com.keila.catalogo.repository.AutorRepository;
import com.keila.catalogo.repository.LibroRepository;
import com.keila.catalogo.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Aplicacion implements CommandLineRunner {

    @Autowired
    private LibroService libroService;
    @Autowired
    private LibroRepository libroRepository;
    @Autowired
    private AutorRepository autorRepository;

    public static void main(String[] args) {
        SpringApplication.run(Aplicacion.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        int opcion = -1;

        do {
            System.out.println("-------------------------");
            System.out.println("Elija la opción a través de su número:");
            System.out.println("1- buscar libro por título");
            System.out.println("2- listar libros registrados");
            System.out.println("3- listar autores registrados");
            System.out.println("4- listar autores vivos en un determinado año");
            System.out.println("5- listar libros por idioma");
            System.out.println("6- cantidad de libros por idioma");
            System.out.println("0- salir");
            System.out.println("-------------------------");

            try {
                String input = sc.nextLine().trim();
                if (input.isEmpty()) continue;
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Opción no válida. Intenta de nuevo.");
                continue;
            }

            try {
                switch (opcion) {
                    case 1 -> {
                        System.out.print("Ingrese el nombre del libro que desea buscar: ");
                        String titulo = sc.nextLine().trim();
                        if (titulo.isBlank()) {
                            System.out.println("⚠ El título no puede estar vacío.");
                            break;
                        }
                        libroService.buscarPorTitulo(titulo);
                    }
                    case 2 -> {
                        if (libroRepository.count() == 0) {
                            System.out.println("No hay libros registrados aún.");
                        } else {
                            libroRepository.findAll().forEach(l ->
                                System.out.printf("- %s | %s | %s | descargas: %d%n",
                                        l.getTitulo(),
                                        l.getAutor() != null ? l.getAutor().getNombre() : "Autor desconocido",
                                        l.getIdioma(),
                                        l.getDescargas() == null ? 0 : l.getDescargas())
                            );
                        }
                    }
                    case 3 -> {
                        if (autorRepository.count() == 0) {
                            System.out.println("No hay autores registrados aún.");
                        } else {
                            autorRepository.findAll().forEach(a ->
                                System.out.printf("- %s (nac: %s, muerte: %s)%n",
                                        a.getNombre(),
                                        a.getAnioNacimiento() == null ? "?" : a.getAnioNacimiento().toString(),
                                        a.getAnioMuerte() == null ? "?" : a.getAnioMuerte().toString())
                            );
                        }
                    }
                    case 4 -> {
                        System.out.print("Ingrese el año: ");
                        try {
                            int anio = Integer.parseInt(sc.nextLine().trim());
                            autorRepository
                                    .findByAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(anio, anio)
                                    .forEach(a -> System.out.println("- " + a.getNombre()));
                        } catch (NumberFormatException e) {
                            System.out.println("⚠ Ingresa un año válido (número).");
                        }
                    }
                    case 5 -> {
                        System.out.print("Ingrese el idioma (ej. en, es, fr): ");
                        String idioma = sc.nextLine().trim();
                        libroRepository.findByIdiomaIgnoreCase(idioma)
                                .forEach(l -> System.out.println("- " + l.getTitulo() + " | " + (l.getAutor() != null ? l.getAutor().getNombre() : "Autor desconocido")));
                    }
                    case 6 -> {
                        System.out.print("Idioma (ej. en, es, fr): ");
                        String idioma = sc.nextLine().trim();
                        long total = libroRepository.countByIdiomaIgnoreCase(idioma);
                        System.out.println("Cantidad: " + total);
                    }
                    case 0 -> System.out.println("👋 Saliendo...");
                    default -> System.out.println("❌ Opción no válida.");
                }
            } catch (Exception e) {
                System.out.println("Ocurrió un error: " + e.getMessage());
            }
        } while (opcion != 0);
    }
}
