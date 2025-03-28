package name.alexkrauss.scouts.infrastructure.dbmock;

import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.application.service.MockResetAware;
import name.alexkrauss.scouts.domain.model.Scout;
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
 * In-memory implementation of ScoutRepository for testing.
 */
@Repository
@Profile("db-mock")
@Primary
public class ScoutRepositoryMock implements ScoutRepository, MockResetAware {

    private final Map<Long, Scout> scouts = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Scout create(Scout scout) {
        Long id = idGenerator.getAndIncrement();
        Scout newScout = scout.toBuilder()
                .id(id)
                .version(0L)
                .build();
        scouts.put(id, newScout);
        return newScout;
    }

    @Override
    public Scout update(Scout scout) {
        Scout existingScout = scouts.get(scout.getId());
        if (existingScout == null) {
            throw new IllegalArgumentException("Scout not found with id: " + scout.getId());
        }

        if (existingScout.getVersion() != scout.getVersion()) {
            throw new OptimisticLockingFailureException("Scout was updated by another transaction");
        }

        Scout updatedScout = scout.toBuilder()
                .version(scout.getVersion() + 1)
                .build();
        scouts.put(updatedScout.getId(), updatedScout);
        return updatedScout;
    }

    @Override
    public void delete(Long id) {
        scouts.remove(id);
    }

    @Override
    public Optional<Scout> findById(Long id) {
        return Optional.ofNullable(scouts.get(id));
    }

    @Override
    public List<Scout> findAll() {
        return new ArrayList<>(scouts.values());
    }

    @Override
    public List<Scout> findByName(String name) {
        return scouts.values().stream()
                .filter(scout -> scout.getName().contains(name))
                .collect(Collectors.toList());
    }

    @Override
    public void reset() {
        scouts.clear();
        idGenerator.set(1);
    }
}