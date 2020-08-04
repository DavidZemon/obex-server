package name.zemon.david.obex.controller;

import name.zemon.david.obex.model.EntryType;
import name.zemon.david.obex.model.TreeEntry;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("api/tree")
public class TreeController {
    private final Path obexPath;

    public TreeController(@Nonnull final Environment environment) {
        this.obexPath = Path.of(environment.getRequiredProperty("obex.local-path"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    public Set<TreeEntry> getTree() throws IOException {
        if (Files.exists(this.obexPath)) {
            final var ignoreList = this.buildIgnoreList(this.obexPath);
            return this.getTree(this.obexPath, ignoreList);
        } else {
            return Collections.emptySet();
        }
    }

    private Set<TreeEntry> getTree(@Nonnull final Path root, @Nonnull final Set<Path> ignoreList) throws IOException {
        final Set<TreeEntry> result = new HashSet<>();

        for (final Path path : Files.list(root).collect(Collectors.toList())) {
            if (!ignoreList.contains(path)) {
                if (Files.isSymbolicLink(path)) {
                    result.add(TreeEntry.builder()
                                   .name(path.getFileName().toString())
                                   .fullPath(this.getFullPath(path))
                                   .type(EntryType.SYMLINK)
                                   .target(path.toRealPath().toString())
                                   .build());
                } else if (Files.isDirectory(path)) {
                    result.add(TreeEntry.builder()
                                   .name(path.getFileName().toString())
                                   .fullPath(this.getFullPath(path))
                                   .type(EntryType.FOLDER)
                                   .children(this.getTree(path, ignoreList))
                                   .build());
                } else if (Files.isRegularFile(path)) {
                    result.add(TreeEntry.builder()
                                   .name(path.getFileName().toString())
                                   .fullPath(this.getFullPath(path))
                                   .type(EntryType.FILE)
                                   .size(Files.size(path))
                                   .build());
                }
            }
        }

        return result;
    }

    private String getFullPath(@Nonnull final Path path) {
        return this.obexPath.relativize(path).toString();
    }

    private Set<Path> buildIgnoreList(@Nonnull final Path root) throws IOException {
        final var ignoreFile = root.resolve(".gitignore");
        final var ignoreList = Files.readAllLines(ignoreFile).stream()
                                   .map(String::strip)
                                   .filter(l -> !l.isEmpty())
                                   .filter(l -> !l.startsWith("#"))
                                   .map(Path::of)
                                   .collect(Collectors.toSet());
        ignoreList.add(root.resolve(".git"));
        return ignoreList;
    }
}
