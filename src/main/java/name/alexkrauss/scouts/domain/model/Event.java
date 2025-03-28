package name.alexkrauss.scouts.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * An event represents a happening that scouts can register for.
 * Events typically involve multiple groups and have a specific location and timeframe.
 */
@Data
@Builder(toBuilder = true)
public class Event {
    Long id;
    long version;

    /**
     * The name of the event, used for display and identification.
     */
    @NonNull
    @NotBlank
    String name;

    /**
     * The start date of the event.
     */
    @NonNull
    @NotNull
    LocalDate startDate;

    /**
     * The end date of the event.
     */
    @NonNull
    @NotNull
    LocalDate endDate;

    /**
     * The meeting point for the event, if specified.
     * This can be empty if attendees should go directly to the location.
     */
    @NonNull
    String meetingPoint;

    /**
     * The location where the event takes place.
     */
    @NonNull
    @NotBlank
    String location;

    /**
     * The groups that can participate in this event.
     * May be empty, in which case all groups can participate.
     */
    @NonNull
    @Builder.Default
    Set<Group> participatingGroups = new HashSet<>();

    /**
     * The cost for participation in the event.
     * This can be empty if there is no cost or if it's determined later.
     */
    @NonNull
    String cost;

    /**
     * Additional information about the event that doesn't fit in other fields.
     */
    @NonNull
    String additionalInfo;
}