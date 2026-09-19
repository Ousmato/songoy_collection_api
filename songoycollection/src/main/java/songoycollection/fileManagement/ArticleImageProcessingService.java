package songoycollection.fileManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ArticleImageProcessingService {

    private final int maxWidth;
    private final int maxHeight;
    private final int thumbnailWidth;
    private final int thumbnailHeight;
    private final int mediumWidth;
    private final int mediumHeight;
    private final int largeWidth;
    private final int largeHeight;
    private final float jpegQuality;
    private final float webpQuality;

    public ArticleImageProcessingService(
            @Value("${app.image.max-width:1600}") int maxWidth,
            @Value("${app.image.max-height:1600}") int maxHeight,
            @Value("${app.image.jpeg-quality:0.82}") float jpegQuality,
            @Value("${app.image.thumbnail.max-width:300}") int thumbnailWidth,
            @Value("${app.image.thumbnail.max-height:300}") int thumbnailHeight,
            @Value("${app.image.medium.max-width:700}") int mediumWidth,
            @Value("${app.image.medium.max-height:700}") int mediumHeight,
            @Value("${app.image.large.max-width:1400}") int largeWidth,
            @Value("${app.image.large.max-height:1400}") int largeHeight,
            @Value("${app.image.webp-quality:0.82}") float webpQuality
    ) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.jpegQuality = jpegQuality;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.mediumWidth = mediumWidth;
        this.mediumHeight = mediumHeight;
        this.largeWidth = largeWidth;
        this.largeHeight = largeHeight;
        this.webpQuality = webpQuality;
    }

    public List<Path> process(Path original) throws IOException {
        String fileName = original.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".gif")) {
            return List.of(original);
        }

        BufferedImage source = ImageIO.read(original.toFile());
        if (source == null) {
            throw new IOException("Format d'image illisible.");
        }

        List<Path> createdFiles = new ArrayList<>();
        try {
            writeJpeg(resize(source, maxWidth, maxHeight), original, jpegQuality);
            createdFiles.add(original);

            // WebP est optionnel : le JDK ne fournit pas d'encodeur WebP par défaut.
            // L'image JPEG principale reste suffisante si aucun plugin WebP n'est installé.
            if (hasWebpEncoder()) {
                Path directory = original.getParent();
                createdFiles.add(writeWebp(source, directory.resolve(stem(original) + "-thumbnail.webp"), thumbnailWidth, thumbnailHeight));
                createdFiles.add(writeWebp(source, directory.resolve(stem(original) + "-medium.webp"), mediumWidth, mediumHeight));
                createdFiles.add(writeWebp(source, directory.resolve(stem(original) + "-large.webp"), largeWidth, largeHeight));
            }
            return createdFiles;
        } catch (IOException | RuntimeException exception) {
            for (Path path : createdFiles) {
                Files.deleteIfExists(path);
            }
            throw exception;
        }
    }

    private void writeJpeg(BufferedImage image, Path path, float quality) throws IOException {
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = rgbImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(quality);
            writer.write(null, new IIOImage(rgbImage, null, null), parameters);
        } finally {
            writer.dispose();
        }
    }

    private Path writeWebp(BufferedImage source, Path path, int width, int height) throws IOException {
        var writers = ImageIO.getImageWritersByFormatName("webp");
        if (!writers.hasNext()) {
            throw new IOException("Aucun encodeur WebP disponible.");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            if (parameters.canWriteCompressed()) {
                parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                parameters.setCompressionQuality(webpQuality);
                writer.write(null, new IIOImage(resize(source, width, height), null, null), parameters);
            } else {
                writer.write(resize(source, width, height));
            }
        } finally {
            writer.dispose();
        }
        return path;
    }

    private boolean hasWebpEncoder() {
        return ImageIO.getImageWritersByFormatName("webp").hasNext();
    }

    private BufferedImage resize(BufferedImage source, int maximumWidth, int maximumHeight) {
        double ratio = Math.min(
                1D,
                Math.min(
                        (double) maximumWidth / source.getWidth(),
                        (double) maximumHeight / source.getHeight()
                )
        );
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private String stem(Path path) {
        String name = path.getFileName().toString();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
