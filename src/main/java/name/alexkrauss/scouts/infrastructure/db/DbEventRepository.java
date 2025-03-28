package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Group;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static name.alexkrauss.scouts.infrastructure.db.generated.tables.EventGroups.EVENT_GROUPS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Events.EVENTS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Groups.GROUPS;

/**
 * jOOQ-based implementation of the EventRepository interface.
 */
@Repository
public class DbEventRepository implements EventRepository {

    private final DSLContext dsl;

    public DbEventRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional
    public Event create(Event event) {
        var record = dsl.insertInto(EVENTS)
                .set(EVENTS.NAME, event.getName())
                .set(EVENTS.START_DATE, event.getStartDate())
                .set(EVENTS.END_DATE, event.getEndDate())
                .set(EVENTS.MEETING_POINT, event.getMeetingPoint())
                .set(EVENTS.LOCATION, event.getLocation())
                .set(EVENTS.COST, event.getCost())
                .set(EVENTS.ADDITIONAL_INFO, event.getAdditionalInfo())
                .set(EVENTS.VERSION, 0L)
                .returning()
                .fetchOne();

        Objects.requireNonNull(record, "Event not created");

        Long eventId = record.getId();
        insertGroups(eventId, event.getParticipatingGroups());

        return event.toBuilder()
                .id(eventId)
                .version(0L)
                .build();
    }

    @Override
    @Transactional
    public Event update(Event event) {
        var updatedRows = dsl.update(EVENTS)
                .set(EVENTS.NAME, event.getName())
                .set(EVENTS.START_DATE, event.getStartDate())
                .set(EVENTS.END_DATE, event.getEndDate())
                .set(EVENTS.MEETING_POINT, event.getMeetingPoint())
                .set(EVENTS.LOCATION, event.getLocation())
                .set(EVENTS.COST, event.getCost())
                .set(EVENTS.ADDITIONAL_INFO, event.getAdditionalInfo())
                .set(EVENTS.VERSION, event.getVersion() + 1)
                .where(EVENTS.ID.eq(event.getId()))
                .and(EVENTS.VERSION.eq(event.getVersion()))
                .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Event was updated by another transaction");
        }

        dsl.deleteFrom(EVENT_GROUPS)
                .where(EVENT_GROUPS.EVENT_ID.eq(event.getId()))
                .execute();

        insertGroups(event.getId(), event.getParticipatingGroups());

        return event.toBuilder()
                .version(event.getVersion() + 1)
                .build();
    }

    @Override
    @Transactional
    public void delete(long id) {
        // group associations are deleted by cascade
        dsl.deleteFrom(EVENTS)
                .where(EVENTS.ID.eq(id))
                .execute();
    }

    @Override
    public Optional<Event> findById(long id) {
        return findEventsByCondition(EVENTS.ID.eq(id))
                .stream()
                .findFirst();
    }

    @Override
    public List<Event> findAll() {
        return findEventsByCondition(null);
    }

    @Override
    public List<Event> findEventsByGroupId(long groupId) {
        // Get events that explicitly include this group
        return findEventsByCondition(
                EVENT_GROUPS.GROUP_ID.eq(groupId)
        );
    }

    private void insertGroups(Long eventId, Set<Group> groups) {
        for (Group group : groups) {
            dsl.insertInto(EVENT_GROUPS)
                    .set(EVENT_GROUPS.EVENT_ID, eventId)
                    .set(EVENT_GROUPS.GROUP_ID, group.getId())
                    .execute();
        }
    }

    private List<Event> findEventsByCondition(Condition condition) {
        var query = dsl.select()
                .from(EVENTS)
                .leftJoin(EVENT_GROUPS).on(EVENTS.ID.eq(EVENT_GROUPS.EVENT_ID))
                .leftJoin(GROUPS).on(EVENT_GROUPS.GROUP_ID.eq(GROUPS.ID));

        if (condition != null) {
            query.where(condition);
        }

        Result<Record> result = query.orderBy(EVENTS.ID).fetch();

        Map<Long, Event.EventBuilder> eventBuilders = new HashMap<>();
        Map<Long, Set<Group>> groups = new HashMap<>();

        for (Record r : result) {
            Long eventId = r.get(EVENTS.ID);

            eventBuilders.computeIfAbsent(eventId, id -> Event.builder()
                    .id(id)
                    .version(r.get(EVENTS.VERSION))
                    .name(r.get(EVENTS.NAME))
                    .startDate(r.get(EVENTS.START_DATE))
                    .endDate(r.get(EVENTS.END_DATE))
                    .meetingPoint(r.get(EVENTS.MEETING_POINT))
                    .location(r.get(EVENTS.LOCATION))
                    .cost(r.get(EVENTS.COST))
                    .additionalInfo(r.get(EVENTS.ADDITIONAL_INFO)));

            if (r.get(GROUPS.ID) != null) {
                groups.computeIfAbsent(eventId, k -> new HashSet<>())
                        .add(Group.builder()
                                .id(r.get(GROUPS.ID))
                                .version(r.get(GROUPS.VERSION))
                                .name(r.get(GROUPS.NAME))
                                .build());
            }
        }

        return eventBuilders.entrySet().stream()
                .map(entry -> entry.getValue()
                        .participatingGroups(groups.getOrDefault(entry.getKey(), new HashSet<>()))
                        .build())
                .toList();
    }
}