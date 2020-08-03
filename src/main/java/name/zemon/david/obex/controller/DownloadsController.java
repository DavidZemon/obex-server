package name.zemon.david.obex.controller;

import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller()
@RequestMapping("downloads")
public class DownloadsController {
    private static final int  PREFIX_LENGTH   = "/downloads/".length();

    private final Path obexPath;

    public DownloadsController(@Nonnull final Environment environment) {
        this.obexPath = Path.of(environment.getRequiredProperty("obex.local-path"));
    }

    @GetMapping("**")
    @Nonnull
    public ResponseEntity<byte[]> getPath(
        @Nonnull final ServletRequest request
    ) throws IOException {
        final var encodedPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                                    .toString();
        final var path = URLDecoder.decode(
            encodedPath.substring(PREFIX_LENGTH),
            Charset.defaultCharset()
        );
        final var absolutePath = this.obexPath.resolve(path);

        final var tempDirectory = Files.createTempDirectory(null);
        try {
            final var resource = new FileSystemResource(this.getPath(absolutePath, tempDirectory));
            final var headers  = new HttpHeaders();
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format(
                    "attachment; filename=\"%s\"",
                    URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), Charset.defaultCharset())
                )
            );
            headers.setContentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM));
            headers.setContentLength(resource.contentLength());
            try (final var stream = resource.getInputStream()) {
                return new ResponseEntity<>(stream.readAllBytes(), headers, HttpStatus.OK);
            }
        } finally {
            try {
                FileSystemUtils.deleteRecursively(tempDirectory);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Nonnull
    private Path getPath(@Nonnull final Path path, @Nonnull final Path tempDir) throws IOException {
        final var pathString = path.toString();
        if (pathString.endsWith(".zip")
                && Files.notExists(path)
                && Files.exists(stripZipSuffix(pathString))) {
            return this.zipDirectory(stripZipSuffix(pathString), tempDir);
        } else {
            return path;
        }
    }

    @Nonnull
    private static Path stripZipSuffix(final String pathString) {
        return Path.of(pathString.substring(0, pathString.length() - 4));
    }

    private Path zipDirectory(@Nonnull final Path inputPath, @Nonnull final Path tempDir) throws IOException {
        final var outputPath = tempDir.resolve(inputPath.getFileName() + ".zip");
        try (final var fos = new FileOutputStream(outputPath.toFile())) {
            try (final var zipOut = new ZipOutputStream(fos)) {
                zipDirectoryEntry(inputPath.toFile(), inputPath.getFileName().toString(), zipOut);
            }
        }
        return outputPath;
    }

    private static void zipDirectoryEntry(
        @Nonnull final File fileToZip,
        @Nonnull final String fileName,
        @Nonnull final ZipOutputStream zipOut
    ) throws IOException {
        if (fileToZip.isDirectory()) {
            zipOut.putNextEntry(new ZipEntry(fileName + (fileName.endsWith("/") ? "" : "/")));
            zipOut.closeEntry();
            final File[] children = fileToZip.listFiles();
            for (final File childFile : Objects.requireNonNull(children)) {
                zipDirectoryEntry(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
        } else {
            try (final var fileStream = new FileInputStream(fileToZip)) {
                zipOut.putNextEntry(new ZipEntry(fileName));

                final var bytes = new byte[4096];
                int       length;
                while ((length = fileStream.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }
        }
    }
}
