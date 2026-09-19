package songoycollection.fileManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileManagerService {
    private final Path uploadRoot;

    public FileManagerService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path save(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Le fichier est vide.");
        }

        String safeFolder = folder == null
                ? "articles"
                : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        Path directory = uploadRoot.resolve(safeFolder).normalize();
        if (!directory.startsWith(uploadRoot)) {
            throw new IOException("Chemin de stockage invalide.");
        }

        Files.createDirectories(directory);
        String originalName = file.getOriginalFilename() == null
                ? "image"
                : file.getOriginalFilename();
        String extension = originalName.matches("(?i).*\\.(jpg|jpeg|png|gif)$")
                ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase()
                : ".bin";
        Path target = directory.resolve(
                System.currentTimeMillis() + "-" + UUID.randomUUID() + extension
        ).normalize();

        if (!target.startsWith(directory)) {
            throw new IOException("Nom de fichier invalide.");
        }

        file.transferTo(target);
        return target;
    }

    public Path resolve(String folder, String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IOException("Nom de fichier invalide.");
        }

        String safeFolder = folder == null
                ? "articles"
                : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        Path directory = uploadRoot.resolve(safeFolder).normalize();
        Path target = directory.resolve(fileName).normalize();
        if (!directory.startsWith(uploadRoot) || !target.startsWith(directory)) {
            throw new IOException("Chemin de fichier invalide.");
        }
        return target;
    }

    public void delete(Path file) throws IOException {
        if (file == null) return;
        Path safe = file.toAbsolutePath().normalize();
        if (!safe.startsWith(uploadRoot)) throw new IOException("Chemin de suppression invalide.");
        Files.deleteIfExists(safe);
    }
}
