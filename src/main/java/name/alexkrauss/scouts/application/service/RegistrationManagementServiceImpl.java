package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.RegistrationManagementService;
import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.RegistrationRepository;
import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Registration;
import name.alexkrauss.scouts.domain.model.Scout;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the RegistrationManagementService interface.
 * Provides functionality for managing registrations of scouts to events.
 */
@Service
@Transactional
public class RegistrationManagementServiceImpl implements RegistrationManagementService {

    private final RegistrationRepository registrationRepository;
    private final ScoutRepository scoutRepository;
    private final EventRepository eventRepository;

    /**
     * Constructs a new RegistrationManagementServiceImpl with the required repositories.
     *
     * @param registrationRepository the repository for managing registrations
     * @param scoutRepository the repository for managing scouts
     * @param eventRepository the repository for managing events
     */
    public RegistrationManagementServiceImpl(RegistrationRepository registrationRepository,
                                          ScoutRepository scoutRepository,
                                          EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.scoutRepository = scoutRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Registration createRegistration(Registration registration) {
        Long scoutId = registration.getScout().getId();
        Long eventId = registration.getEvent().getId();
        
        // Verify that the scout exists
        verifyScoutExists(scoutId);
        
        // Verify that the event exists
        verifyEventExists(eventId);
        
        // Check for duplicate registration
        if (registrationRepository.existsByEventIdAndScoutId(eventId, scoutId)) {
            throw new IllegalArgumentException("Scout is already registered for this event");
        }
        
        return registrationRepository.create(registration);
    }

    @Override
    public Registration updateRegistration(Registration registration) {
        // Get the existing registration
        Registration existingRegistration = getRegistration(registration.getId());
        
        // Ensure scout and event are not changed
        if (!existingRegistration.getScout().getId().equals(registration.getScout().getId())) {
            throw new IllegalArgumentException("Cannot change the scout of an existing registration");
        }
        
        if (!existingRegistration.getEvent().getId().equals(registration.getEvent().getId())) {
            throw new IllegalArgumentException("Cannot change the event of an existing registration");
        }
        
        return registrationRepository.update(registration);
    }

    @Override
    public void deleteRegistration(Long registrationId) {
        // Verify that the registration exists
        getRegistration(registrationId);
        
        registrationRepository.delete(registrationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Registration getRegistration(Long registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration with id " + registrationId + " does not exist"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByEvent(Long eventId) {
        // Verify that the event exists
        verifyEventExists(eventId);
        
        return registrationRepository.findByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByScout(Long scoutId) {
        // Verify that the scout exists
        verifyScoutExists(scoutId);
        
        return registrationRepository.findByScoutId(scoutId);
    }

    /**
     * Verifies that a scout with the given ID exists.
     *
     * @param scoutId The scout ID to verify.
     * @throws IllegalArgumentException if the scout doesn't exist.
     */
    private void verifyScoutExists(Long scoutId) {
        scoutRepository.findById(scoutId)
                .orElseThrow(() -> new IllegalArgumentException("Scout with id " + scoutId + " does not exist"));
    }

    /**
     * Verifies that an event with the given ID exists.
     *
     * @param eventId The event ID to verify.
     * @throws IllegalArgumentException if the event doesn't exist.
     */
    private void verifyEventExists(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " does not exist"));
    }
}