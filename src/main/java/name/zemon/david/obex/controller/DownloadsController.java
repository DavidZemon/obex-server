package name.zemon.david.obex.controller;

import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;

@Controller()
@RequestMapping("downloads")
public class DownloadsController {
    private static final int PREFIX_LENGTH = "/downloads/".length();

    private final Path obexPath;

    public DownloadsController(@Nonnull final Environment environment) {
        this.obexPath = Path.of(environment.getRequiredProperty("obex.local-path"));
    }

    @GetMapping("**")
    @Nonnull
    public ResponseEntity<FileSystemResource> getPath(
        @Nonnull final ServletRequest request
    ) throws IOException {
        final var encodedPath  = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                                     .toString();
        final var path         = URLDecoder.decode(
            encodedPath.substring(PREFIX_LENGTH),
            Charset.defaultCharset()
        );
        final var absolutePath = this.obexPath.resolve(path);

        final var resource = new FileSystemResource(absolutePath);
        final var headers  = new HttpHeaders();
        headers.set(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format(
                "attachment; filename=\"%s\"",
                URLEncoder.encode(absolutePath.getFileName().toString(), Charset.defaultCharset())
            )
        );
        headers.setContentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM));
        headers.setContentLength(resource.contentLength());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
