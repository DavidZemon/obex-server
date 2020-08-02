package name.zemon.david.obex.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Data
@Builder
public class TreeEntry {
  @Nonnull
  private String name;
  @Nonnull
  private EntryType type;
  @Nullable
  private Long size;
  @Nullable
  private Set<TreeEntry> children;
}
