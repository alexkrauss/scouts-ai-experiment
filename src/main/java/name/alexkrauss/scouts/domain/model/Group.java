package name.alexkrauss.scouts.domain.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * A group represents an organizational unit within the scout organization
 * that participates in events.
 */
@Data
@Builder(toBuilder = true)
public class Group {
    Long id;
    long version;

    /**
     * The identifying name of the group within the organization.
     *
     * Used for display, but we don't enforce uniqueness.
     */
    @NonNull
    @NotEmpty
    String name;
}
