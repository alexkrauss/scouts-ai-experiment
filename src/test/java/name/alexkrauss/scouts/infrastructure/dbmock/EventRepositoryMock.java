package name.alexkrauss.scouts.infrastructure.dbmock;

import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.service.MockResetAware;
import name.alexkrauss.scouts.domain.model.Event;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of EventRepository for testing.
 */
@Repository
@Profile("db-mock")
@Primary
public class EventRepositoryMock implements EventRepository, MockResetAware {

    private final Map<Long, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Event create(Event event) {
        Long id = idGenerator.getAndIncrement();
        Event newEvent = event.toBuilder()
                .id(id)
                .version(0L)
                .build();
        events.put(id, newEvent);
        return newEvent;
    }

    @Override
    public Event update(Event event) {
        Event existingEvent = events.get(event.getId());
        if (existingEvent == null) {
            throw new IllegalArgumentException("Event not found with id: " + event.getId());
        }

        if (existingEvent.getVersion() != event.getVersion()) {
            throw new OptimisticLockingFailureException("Event was updated by another transaction");
        }

        Event updatedEvent = event.toBuilder()
                .version(event.getVersion() + 1)
                .build();
        events.put(updatedEvent.getId(), updatedEvent);
        return updatedEvent;
    }

    @Override
    public void delete(long id) {
        events.remove(id);
    }

    @Override
    public Optional<Event> findById(long id) {
        return Optional.ofNullable(events.get(id));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    @Override
    public List<Event> findEventsByGroupId(long groupId) {
        // Get events that explicitly include this group
        return events.values().stream()
                .filter(event -> event.getParticipatingGroups().stream()
                        .anyMatch(group -> group.getId() == groupId))
                .collect(Collectors.toList());
    }

    @Override
    public void reset() {
        events.clear();
        idGenerator.set(1);
    }
}