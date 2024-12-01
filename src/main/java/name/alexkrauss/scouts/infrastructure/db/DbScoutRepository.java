package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Contact;
import name.alexkrauss.scouts.domain.model.Group;
import name.alexkrauss.scouts.domain.model.Scout;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static name.alexkrauss.scouts.infrastructure.db.generated.tables.ScoutContacts.SCOUT_CONTACTS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.ScoutGroups.SCOUT_GROUPS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Scouts.SCOUTS;
import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Groups.GROUPS;

@Repository
public class DbScoutRepository implements ScoutRepository {

    private final DSLContext dsl;

    public DbScoutRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional
    public Scout create(Scout scout) {
        var record = dsl.insertInto(SCOUTS)
                .set(SCOUTS.NAME, scout.getName())
                .set(SCOUTS.BIRTH_DATE, scout.getBirthDate())
                .set(SCOUTS.ADDRESS, scout.getAddress())
                .set(SCOUTS.PHONE_NUMBER, scout.getPhoneNumber())
                .set(SCOUTS.HEALTH_INSURANCE, scout.getHealthInsurance())
                .set(SCOUTS.ALLERGY_INFO, scout.getAllergyInfo())
                .set(SCOUTS.VACCINATION_INFO, scout.getVaccinationInfo())
                .set(SCOUTS.LAST_UPDATED, scout.getLastUpdated())
                .set(SCOUTS.VERSION, 0L)
                .returning()
                .fetchOne();

        Objects.requireNonNull(record, "Scout not created");

        Long scoutId = record.getId();
        insertContacts(scoutId, scout.getContacts());
        insertGroups(scoutId, scout.getGroups());

        return scout.toBuilder()
                .id(scoutId)
                .version(0L)
                .build();
    }

    @Override
    @Transactional
    public Scout update(Scout scout) {
        var updatedRows = dsl.update(SCOUTS)
                .set(SCOUTS.NAME, scout.getName())
                .set(SCOUTS.BIRTH_DATE, scout.getBirthDate())
                .set(SCOUTS.ADDRESS, scout.getAddress())
                .set(SCOUTS.PHONE_NUMBER, scout.getPhoneNumber())
                .set(SCOUTS.HEALTH_INSURANCE, scout.getHealthInsurance())
                .set(SCOUTS.ALLERGY_INFO, scout.getAllergyInfo())
                .set(SCOUTS.VACCINATION_INFO, scout.getVaccinationInfo())
                .set(SCOUTS.LAST_UPDATED, scout.getLastUpdated())
                .set(SCOUTS.VERSION, scout.getVersion() + 1)
                .where(SCOUTS.ID.eq(scout.getId()))
                .and(SCOUTS.VERSION.eq(scout.getVersion()))
                .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Scout was updated by another transaction");
        }

        dsl.deleteFrom(SCOUT_CONTACTS)
                .where(SCOUT_CONTACTS.SCOUT_ID.eq(scout.getId()))
                .execute();

        dsl.deleteFrom(SCOUT_GROUPS)
                .where(SCOUT_GROUPS.SCOUT_ID.eq(scout.getId()))
                .execute();

        insertContacts(scout.getId(), scout.getContacts());
        insertGroups(scout.getId(), scout.getGroups());

        return scout.toBuilder()
                .version(scout.getVersion() + 1)
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // group associations and contacts are deleted by cascade

        dsl.deleteFrom(SCOUTS)
                .where(SCOUTS.ID.eq(id))
                .execute();
    }

    @Override
    public Optional<Scout> findById(Long id) {
        return findScoutsByCondition(SCOUTS.ID.eq(id))
                .stream()
                .findFirst();
    }

    @Override
    public List<Scout> findAll() {
        return findScoutsByCondition(null);
    }

    @Override
    public List<Scout> findByName(String name) {
        return findScoutsByCondition(SCOUTS.NAME.eq(name));
    }

    private void insertContacts(Long scoutId, List<Contact> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            dsl.insertInto(SCOUT_CONTACTS)
                    .set(SCOUT_CONTACTS.SCOUT_ID, scoutId)
                    .set(SCOUT_CONTACTS.CONTACT_ORDER, i)
                    .set(SCOUT_CONTACTS.NAME, contact.name())
                    .set(SCOUT_CONTACTS.PHONE_NUMBER, contact.phoneNumber())
                    .set(SCOUT_CONTACTS.EMAIL, contact.email())
                    .set(SCOUT_CONTACTS.RELATIONSHIP, contact.relationship())
                    .execute();
        }
    }

    private void insertGroups(Long scoutId, Set<Group> groups) {
        for (Group group : groups) {
            dsl.insertInto(SCOUT_GROUPS)
                    .set(SCOUT_GROUPS.SCOUT_ID, scoutId)
                    .set(SCOUT_GROUPS.GROUP_ID, group.getId())
                    .execute();
        }
    }

    private List<Scout> findScoutsByCondition(org.jooq.Condition condition) {
        var query = dsl.select()
                .from(SCOUTS)
                .leftJoin(SCOUT_CONTACTS).on(SCOUTS.ID.eq(SCOUT_CONTACTS.SCOUT_ID))
                .leftJoin(SCOUT_GROUPS).on(SCOUTS.ID.eq(SCOUT_GROUPS.SCOUT_ID))
                .leftJoin(GROUPS).on(SCOUT_GROUPS.GROUP_ID.eq(GROUPS.ID));

        if (condition != null) {
            query.where(condition);
        }

        Result<Record> result = query.orderBy(SCOUTS.ID, SCOUT_CONTACTS.CONTACT_ORDER).fetch();

        Map<Long, Scout.ScoutBuilder> scoutBuilders = new HashMap<>();
        Map<Long, List<Contact>> contacts = new HashMap<>();
        Map<Long, Set<Group>> groups = new HashMap<>();

        for (Record r : result) {
            Long scoutId = r.get(SCOUTS.ID);

            scoutBuilders.computeIfAbsent(scoutId, id -> Scout.builder()
                    .id(id)
                    .version(r.get(SCOUTS.VERSION))
                    .name(r.get(SCOUTS.NAME))
                    .birthDate(r.get(SCOUTS.BIRTH_DATE))
                    .address(r.get(SCOUTS.ADDRESS))
                    .phoneNumber(r.get(SCOUTS.PHONE_NUMBER))
                    .healthInsurance(r.get(SCOUTS.HEALTH_INSURANCE))
                    .allergyInfo(r.get(SCOUTS.ALLERGY_INFO))
                    .vaccinationInfo(r.get(SCOUTS.VACCINATION_INFO))
                    .lastUpdated(r.get(SCOUTS.LAST_UPDATED)));

            if (r.get(SCOUT_CONTACTS.SCOUT_ID) != null) {
                contacts.computeIfAbsent(scoutId, k -> new ArrayList<>())
                        .add(Contact.builder()
                                .name(r.get(SCOUT_CONTACTS.NAME))
                                .phoneNumber(r.get(SCOUT_CONTACTS.PHONE_NUMBER))
                                .email(r.get(SCOUT_CONTACTS.EMAIL))
                                .relationship(r.get(SCOUT_CONTACTS.RELATIONSHIP))
                                .build());
            }

            if (r.get(GROUPS.ID) != null) {
                groups.computeIfAbsent(scoutId, k -> new HashSet<>())
                        .add(Group.builder()
                                .id(r.get(GROUPS.ID))
                                .version(r.get(GROUPS.VERSION))
                                .name(r.get(GROUPS.NAME))
                                .build());
            }
        }

        return scoutBuilders.entrySet().stream()
                .map(entry -> entry.getValue()
                        .contacts(contacts.getOrDefault(entry.getKey(), new ArrayList<>()))
                        .groups(groups.getOrDefault(entry.getKey(), new HashSet<>()))
                        .build())
                .toList();
    }
}
