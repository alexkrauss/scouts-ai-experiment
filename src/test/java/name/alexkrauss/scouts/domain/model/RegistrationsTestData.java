package name.alexkrauss.scouts.domain.model;

import java.time.LocalDateTime;

import static name.alexkrauss.scouts.domain.model.EventsTestData.SUMMER_CAMP;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.JOHN_DOE;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.EMMA_SMITH;

/**
 * Test data for Registration entities.
 */
public class RegistrationsTestData {

    public static final Registration JOHN_SUMMER_CAMP = Registration.builder()
            .scout(JOHN_DOE)
            .event(SUMMER_CAMP)
            .note("Vegetarian food required")
            .status(RegistrationStatus.PENDING)
            .registrationDate(LocalDateTime.of(2025, 2, 15, 14, 30))
            .accountId("parent1")
            .build();

    public static final Registration EMMA_SUMMER_CAMP = Registration.builder()
            .scout(EMMA_SMITH)
            .event(SUMMER_CAMP)
            .note("Will need medical attention for allergies")
            .status(RegistrationStatus.CONFIRMED)
            .registrationDate(LocalDateTime.of(2025, 2, 10, 9, 15))
            .accountId("parent2")
            .build();
}