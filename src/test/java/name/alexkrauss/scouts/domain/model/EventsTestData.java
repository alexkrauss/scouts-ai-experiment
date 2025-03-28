package name.alexkrauss.scouts.domain.model;

import java.time.LocalDate;

/**
 * Test data for Event entities.
 */
public class EventsTestData {

    public static final Event SUMMER_CAMP = Event.builder()
            .name("Summer Camp 2025")
            .startDate(LocalDate.of(2025, 7, 15))
            .endDate(LocalDate.of(2025, 7, 22))
            .meetingPoint("Central Station, Platform 3")
            .location("Lake Forest Campground")
            .cost("250€")
            .additionalInfo("Bring sleeping bag and tent")
            .build();

    public static final Event HIKING_TRIP = Event.builder()
            .name("Mountain Hiking Trip")
            .startDate(LocalDate.of(2025, 5, 10))
            .endDate(LocalDate.of(2025, 5, 12))
            .meetingPoint("Scout House")
            .location("Alpine Mountain Range")
            .cost("100€")
            .additionalInfo("Appropriate hiking gear required")
            .build();

    public static final Event CRAFT_WORKSHOP = Event.builder()
            .name("Craft Workshop")
            .startDate(LocalDate.of(2025, 3, 20))
            .endDate(LocalDate.of(2025, 3, 20))
            .meetingPoint("")
            .location("Scout House")
            .cost("15€")
            .additionalInfo("All materials provided")
            .build();
}