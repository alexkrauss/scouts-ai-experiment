package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.RegistrationManagementService;
import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Registration;
import name.alexkrauss.scouts.domain.model.RegistrationStatus;
import name.alexkrauss.scouts.domain.model.Scout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;
import java.util.List;

import static name.alexkrauss.scouts.domain.model.EventsTestData.SUMMER_CAMP;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.JOHN_DOE;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.EMMA_SMITH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the RegistrationManagementService implementation.
 * These tests verify that the service correctly manages registrations of scouts to events.
 */
@SpringBootTest
@ActiveProfiles("db-mock")
@ContextConfiguration(classes = MockedDbTestConfiguration.class)
@TestExecutionListeners(
        listeners = MockedDbTestConfiguration.DbMockResetTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RegistrationManagementServiceTest {

    @Autowired
    private RegistrationManagementService service;

    @Autowired
    private ScoutRepository scoutRepository;

    @Autowired
    private EventRepository eventRepository;

    private Scout johnDoe;
    private Scout emmaSmith;
    private Event summerCamp;

    @BeforeEach
    void setUp() {
        // Create test scouts
        johnDoe = scoutRepository.create(JOHN_DOE);
        emmaSmith = scoutRepository.create(EMMA_SMITH);
        
        // Create test event
        summerCamp = eventRepository.create(SUMMER_CAMP);
    }

    /**
     * Tests that a registration can be created and then retrieved by its ID.
     * Verifies that:
     * - The created registration receives an ID
     * - The retrieved registration matches the input registration
     */
    @Test
    void createAndRetrieveRegistration() {
        Registration registration = buildTestRegistration(johnDoe, summerCamp);
        Registration savedRegistration = service.createRegistration(registration);
        
        assertThat(savedRegistration.getId()).isNotNull();
        
        Registration retrievedRegistration = service.getRegistration(savedRegistration.getId());
        assertThat(retrievedRegistration).usingRecursiveComparison()
                .ignoringFields("id", "version")
                .isEqualTo(registration);
        assertThat(retrievedRegistration.getId()).isEqualTo(savedRegistration.getId());
        assertThat(retrievedRegistration.getVersion()).isEqualTo(0);
    }

    /**
     * Tests that a registration can be updated.
     * Verifies that:
     * - The updated registration properties are correctly persisted
     * - The version is incremented
     * - The scout and event cannot be changed
     */
    @Test
    void updateRegistration() {
        Registration registration = service.createRegistration(buildTestRegistration(johnDoe, summerCamp));
        
        Registration updatedRegistration = service.updateRegistration(registration.toBuilder()
                .note("Updated note information")
                .status(RegistrationStatus.CONFIRMED)
                .build());
        
        // Verify note and status were updated
        assertThat(updatedRegistration.getNote()).isEqualTo("Updated note information");
        assertThat(updatedRegistration.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(updatedRegistration.getVersion()).isEqualTo(1);
        
        // Verify scout and event remain unchanged
        assertThat(updatedRegistration.getScout().getId()).isEqualTo(johnDoe.getId());
        assertThat(updatedRegistration.getEvent().getId()).isEqualTo(summerCamp.getId());
        
        Registration retrievedRegistration = service.getRegistration(registration.getId());
        assertThat(retrievedRegistration).usingRecursiveComparison()
                .isEqualTo(updatedRegistration);
    }

    /**
     * Tests that a registration can be deleted.
     * Verifies that:
     * - After deletion, trying to retrieve the registration throws an exception
     */
    @Test
    void deleteRegistration() {
        Registration registration = service.createRegistration(buildTestRegistration(johnDoe, summerCamp));
        
        service.deleteRegistration(registration.getId());
        
        assertThatThrownBy(() -> service.getRegistration(registration.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }

    /**
     * Tests retrieving registrations by event.
     * Verifies that:
     * - Only registrations for the given event are returned
     */
    @Test
    void getRegistrationsByEvent() {
        // Create another event for contrast
        Event anotherEvent = eventRepository.create(SUMMER_CAMP.toBuilder().name("Another Event").build());
        
        // Create registrations for different events
        Registration reg1 = service.createRegistration(buildTestRegistration(johnDoe, summerCamp));
        Registration reg2 = service.createRegistration(buildTestRegistration(emmaSmith, summerCamp));
        Registration reg3 = service.createRegistration(buildTestRegistration(johnDoe, anotherEvent));
        
        List<Registration> registrations = service.getRegistrationsByEvent(summerCamp.getId());
        
        assertThat(registrations).hasSize(2);
        assertThat(registrations).extracting("id")
                .containsExactlyInAnyOrder(reg1.getId(), reg2.getId());
    }

    /**
     * Tests retrieving registrations by scout.
     * Verifies that:
     * - Only registrations for the given scout are returned
     */
    @Test
    void getRegistrationsByScout() {
        // Create registrations for different scouts
        Registration reg1 = service.createRegistration(buildTestRegistration(johnDoe, summerCamp));
        
        // Create another event for additional registrations
        Event anotherEvent = eventRepository.create(SUMMER_CAMP.toBuilder().name("Another Event").build());
        Registration reg2 = service.createRegistration(buildTestRegistration(johnDoe, anotherEvent));
        Registration reg3 = service.createRegistration(buildTestRegistration(emmaSmith, summerCamp));
        
        List<Registration> registrations = service.getRegistrationsByScout(johnDoe.getId());
        
        assertThat(registrations).hasSize(2);
        assertThat(registrations).extracting("id")
                .containsExactlyInAnyOrder(reg1.getId(), reg2.getId());
    }

    /**
     * Tests handling of non-existent registration ID.
     * Verifies that:
     * - Appropriate exceptions are thrown when trying to operate on non-existent registrations
     */
    @Test
    void handleNonExistentRegistration() {
        Long nonExistentId = 999L;
        
        // Test getRegistration
        assertThatThrownBy(() -> service.getRegistration(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        
        // Test deleteRegistration
        assertThatThrownBy(() -> service.deleteRegistration(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }
    
    /**
     * Tests that attempting to register the same scout for the same event twice
     * results in an appropriate exception.
     * Verifies that:
     * - The first registration succeeds
     * - The second duplicate registration fails with the correct error message
     */
    @Test
    void rejectDuplicateRegistration() {
        // Register John for summer camp
        Registration firstRegistration = buildTestRegistration(johnDoe, summerCamp);
        service.createRegistration(firstRegistration);
        
        // Try to register John for summer camp again
        Registration duplicateRegistration = buildTestRegistration(johnDoe, summerCamp);
        
        assertThatThrownBy(() -> service.createRegistration(duplicateRegistration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scout is already registered for this event");
    }
    
    /**
     * Tests that retrieving registrations for a non-existent event throws an exception.
     * Verifies that:
     * - Attempting to get registrations for a deleted event results in an appropriate exception
     */
    @Test
    void getRegistrationsForDeletedEvent() {
        // Create and then delete an event
        Event tempEvent = eventRepository.create(SUMMER_CAMP.toBuilder().name("Temporary Event").build());
        eventRepository.delete(tempEvent.getId());
        
        // Attempt to get registrations for the deleted event
        assertThatThrownBy(() -> service.getRegistrationsByEvent(tempEvent.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event with id " + tempEvent.getId() + " does not exist");
    }
    
    /**
     * Tests that retrieving registrations for a non-existent scout throws an exception.
     * Verifies that:
     * - Attempting to get registrations for a deleted scout results in an appropriate exception
     */
    @Test
    void getRegistrationsForDeletedScout() {
        // Create and then delete a scout
        Scout tempScout = scoutRepository.create(JOHN_DOE.toBuilder().name("Temporary Scout").build());
        scoutRepository.delete(tempScout.getId());
        
        // Attempt to get registrations for the deleted scout
        assertThatThrownBy(() -> service.getRegistrationsByScout(tempScout.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scout with id " + tempScout.getId() + " does not exist");
    }

    /**
     * Helper method to build a test registration.
     */
    private Registration buildTestRegistration(Scout scout, Event event) {
        return Registration.builder()
                .scout(scout)
                .event(event)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build();
    }
}