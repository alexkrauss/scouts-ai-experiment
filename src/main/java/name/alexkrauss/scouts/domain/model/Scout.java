package name.alexkrauss.scouts.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A scout is a member of the organization who can register for events.
 *
 * We refrain from overspecifying constraints, since most of the fields are not
 * used formally.
 */
@Data
@Builder(toBuilder = true)
public class Scout {
    Long id;
    long version;

    /**
     * The groups the scout belongs to. May be empty.
     */
    @NonNull
    @Builder.Default
    Set<Group> groups = new HashSet<>();

    /**
     * Full name of the scout, used for identification and communication.
     */
    @NonNull
    @NotBlank
    String name;

    /**
     * Date of birth of the scout, used to calculate age.
     */
    @NonNull
    LocalDate birthDate;

    /**
     * Home address of the scout.
     */
    @NonNull
    @NotBlank
    String address;

    /**
     * Direct contact number for the scout, if available.
     */
    @NonNull
    String phoneNumber;

    /**
     * Healthcare provider details for emergency situations.
     */
    @NonNull
    @NotBlank
    String healthInsurance;

    /**
     * Medical allergy information needed for safe participation in events.
     */
    @NonNull
    String allergyInfo;

    /**
     * Record of immunizations relevant for event participation.
     */
    @NonNull
    String vaccinationInfo;

    /**
     * Emergency contacts and guardians who can make decisions for the scout.
     */
    @NonNull
    @NotEmpty
    @Builder.Default
    List<Contact> contacts = new ArrayList<>();

    /**
     * Timestamp of the most recent data verification or update.
     */
    @NonNull
    LocalDate lastUpdated;
}
