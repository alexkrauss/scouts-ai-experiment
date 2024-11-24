package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Group;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static name.alexkrauss.scouts.infrastructure.db.generated.tables.Groups.GROUPS;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

    private final DSLContext dsl;

    public GroupRepositoryImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Group create(Group group) {
        var record = dsl.insertInto(GROUPS)
                .set(GROUPS.NAME, group.getName())
                .set(GROUPS.VERSION, 0L)
                .returning()
                .fetchOne();

        Objects.requireNonNull(record, "Group not created");

        return Group.builder()
                .id(record.getId())
                .name(record.getName())
                .version(record.getVersion())
                .build();
    }

    @Override
    public Group update(Group group) {
        var updatedRows = dsl.update(GROUPS)
                .set(GROUPS.NAME, group.getName())
                .set(GROUPS.VERSION, group.getVersion() + 1)
                .where(GROUPS.ID.eq(group.getId()))
                .and(GROUPS.VERSION.eq(group.getVersion()))
                .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Group was updated by another transaction");
        }

        return group.toBuilder()
                .version(group.getVersion() + 1)
                .build();
    }

    @Override
    public void delete(long id) {
        dsl.deleteFrom(GROUPS)
                .where(GROUPS.ID.eq(id))
                .execute();
    }

    @Override
    public Optional<Group> findById(long id) {
        return dsl.selectFrom(GROUPS)
                .where(GROUPS.ID.eq(id))
                .fetchOptional()
                .map(record -> Group.builder()
                        .id(record.getId())
                        .name(record.getName())
                        .version(record.getVersion())
                        .build());
    }

    @Override
    public List<Group> findAll() {
        return dsl.selectFrom(GROUPS)
                .fetch()
                .map(record -> Group.builder()
                        .id(record.getId())
                        .name(record.getName())
                        .version(record.getVersion())
                        .build());
    }
}
