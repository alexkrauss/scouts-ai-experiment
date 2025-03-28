package name.alexkrauss.scouts.infrastructure.dbmock;

import name.alexkrauss.scouts.application.ports.persistence.RegistrationRepository;
import name.alexkrauss.scouts.application.service.MockResetAware;
import name.alexkrauss.scouts.domain.model.Registration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of RegistrationRepository for testing.
 */
@Repository
@Profile("db-mock")
@Primary
public class RegistrationRepositoryMock implements RegistrationRepository, MockResetAware {

    private final Map<Long, Registration> registrations = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Registration create(Registration registration) {
        Long id = idGenerator.getAndIncrement();
        Registration newRegistration = registration.toBuilder()
                .id(id)
                .version(0L)
                .build();
        registrations.put(id, newRegistration);
        return newRegistration;
    }

    @Override
    public Registration update(Registration registration) {
        Registration existingRegistration = registrations.get(registration.getId());
        if (existingRegistration == null) {
            throw new IllegalArgumentException("Registration not found with id: " + registration.getId());
        }

        if (existingRegistration.getVersion() != registration.getVersion()) {
            throw new OptimisticLockingFailureException("Registration was updated by another transaction");
        }

        Registration updatedRegistration = registration.toBuilder()
                .version(registration.getVersion() + 1)
                .build();
        registrations.put(updatedRegistration.getId(), updatedRegistration);
        return updatedRegistration;
    }

    @Override
    public void delete(long id) {
        registrations.remove(id);
    }

    @Override
    public Optional<Registration> findById(long id) {
        return Optional.ofNullable(registrations.get(id));
    }

    @Override
    public List<Registration> findByEventId(long eventId) {
        return registrations.values().stream()
                .filter(registration -> registration.getEvent().getId() == eventId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByScoutId(long scoutId) {
        return registrations.values().stream()
                .filter(registration -> registration.getScout().getId() == scoutId)
                .collect(Collectors.toList());
    }

    @Override
    public void reset() {
        registrations.clear();
        idGenerator.set(1);
    }
}