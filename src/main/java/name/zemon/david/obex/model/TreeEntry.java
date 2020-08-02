package name.zemon.david.obex.model;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@Data
@Builder
public class TreeEntry {
    @Nonnull
    private String         name;
    @Nonnull
    private String         fullPath;
    @Nonnull
    private EntryType      type;
    @Nullable
    private Long           size;
    @Nullable
    private Set<TreeEntry> children;
    @Nullable
    private String         target;
}
