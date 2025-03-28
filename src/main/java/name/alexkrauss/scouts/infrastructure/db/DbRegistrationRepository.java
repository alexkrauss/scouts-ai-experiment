package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.RegistrationRepository;
import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Registration;
import name.alexkrauss.scouts.domain.model.RegistrationStatus;
import name.alexkrauss.scouts.domain.model.Scout;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Events.EVENTS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Registrations.REGISTRATIONS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Scouts.SCOUTS;

/**
 * jOOQ-based implementation of the RegistrationRepository interface.
 */
@Repository
public class DbRegistrationRepository implements RegistrationRepository {

    private final DSLContext dsl;
    private final ScoutRepository scoutRepository;
    private final EventRepository eventRepository;

    public DbRegistrationRepository(DSLContext dsl, ScoutRepository scoutRepository, EventRepository eventRepository) {
        this.dsl = dsl;
        this.scoutRepository = scoutRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Registration create(Registration registration) {
        var record = dsl.insertInto(REGISTRATIONS)
                .set(REGISTRATIONS.SCOUT_ID, registration.getScout().getId())
                .set(REGISTRATIONS.EVENT_ID, registration.getEvent().getId())
                .set(REGISTRATIONS.NOTE, registration.getNote())
                .set(REGISTRATIONS.STATUS, registration.getStatus().name())
                .set(REGISTRATIONS.REGISTRATION_DATE, registration.getRegistrationDate())
                .set(REGISTRATIONS.ACCOUNT_ID, registration.getAccountId())
                .set(REGISTRATIONS.VERSION, 0L)
                .returning()
                .fetchOne();

        if (record == null) {
            throw new IllegalStateException("Failed to create registration");
        }

        return registration.toBuilder()
                .id(record.getId())
                .version(0L)
                .build();
    }

    @Override
    @Transactional
    public Registration update(Registration registration) {
        var updatedRows = dsl.update(REGISTRATIONS)
                .set(REGISTRATIONS.SCOUT_ID, registration.getScout().getId())
                .set(REGISTRATIONS.EVENT_ID, registration.getEvent().getId())
                .set(REGISTRATIONS.NOTE, registration.getNote())
                .set(REGISTRATIONS.STATUS, registration.getStatus().name())
                .set(REGISTRATIONS.REGISTRATION_DATE, registration.getRegistrationDate())
                .set(REGISTRATIONS.ACCOUNT_ID, registration.getAccountId())
                .set(REGISTRATIONS.VERSION, registration.getVersion() + 1)
                .where(REGISTRATIONS.ID.eq(registration.getId()))
                .and(REGISTRATIONS.VERSION.eq(registration.getVersion()))
                .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Registration was updated by another transaction");
        }

        return registration.toBuilder()
                .version(registration.getVersion() + 1)
                .build();
    }

    @Override
    @Transactional
    public void delete(long id) {
        dsl.deleteFrom(REGISTRATIONS)
                .where(REGISTRATIONS.ID.eq(id))
                .execute();
    }

    @Override
    public Optional<Registration> findById(long id) {
        return findRegistrationsByCondition(REGISTRATIONS.ID.eq(id))
                .stream()
                .findFirst();
    }

    @Override
    public List<Registration> findByEventId(long eventId) {
        return findRegistrationsByCondition(REGISTRATIONS.EVENT_ID.eq(eventId));
    }

    @Override
    public List<Registration> findByScoutId(long scoutId) {
        return findRegistrationsByCondition(REGISTRATIONS.SCOUT_ID.eq(scoutId));
    }

    private List<Registration> findRegistrationsByCondition(Condition condition) {
        Result<Record> result = dsl.select()
                .from(REGISTRATIONS)
                .join(SCOUTS).on(REGISTRATIONS.SCOUT_ID.eq(SCOUTS.ID))
                .join(EVENTS).on(REGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
                .where(condition)
                .orderBy(REGISTRATIONS.ID)
                .fetch();

        Map<Long, Registration> registrations = new HashMap<>();

        for (Record r : result) {
            Long registrationId = r.get(REGISTRATIONS.ID);
            
            // If we already processed this registration, skip
            if (registrations.containsKey(registrationId)) {
                continue;
            }
            
            // Fetch the complete scout and event objects
            Scout scout = scoutRepository.findById(r.get(REGISTRATIONS.SCOUT_ID)).orElseThrow();
            Event event = eventRepository.findById(r.get(REGISTRATIONS.EVENT_ID)).orElseThrow();

            Registration registration = Registration.builder()
                    .id(registrationId)
                    .version(r.get(REGISTRATIONS.VERSION))
                    .scout(scout)
                    .event(event)
                    .note(r.get(REGISTRATIONS.NOTE))
                    .status(RegistrationStatus.valueOf(r.get(REGISTRATIONS.STATUS)))
                    .registrationDate(r.get(REGISTRATIONS.REGISTRATION_DATE))
                    .accountId(r.get(REGISTRATIONS.ACCOUNT_ID))
                    .build();

            registrations.put(registrationId, registration);
        }

        return registrations.values().stream().toList();
    }
}