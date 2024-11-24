package name.alexkrauss.scouts.infrastructure.rest.controller;

import name.alexkrauss.scouts.application.ports.api.GroupManagementService;
import name.alexkrauss.scouts.infrastructure.rest.api.GroupsApi;
import name.alexkrauss.scouts.infrastructure.rest.model.Group;
import name.alexkrauss.scouts.infrastructure.rest.model.GroupRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GroupsController implements GroupsApi {

    private final GroupManagementService groupManagementService;

    public GroupsController(GroupManagementService groupManagementService) {
        this.groupManagementService = groupManagementService;
    }

    @Override
    public ResponseEntity<Group> createGroup(GroupRequest groupRequest) {
        var domainGroup = name.alexkrauss.scouts.domain.model.Group.builder()
                .name(groupRequest.getName())
                .build();

        var createdGroup = groupManagementService.createGroup(domainGroup);
        return ResponseEntity.status(201).body(mapToApiGroup(createdGroup));
    }

    @Override
    public ResponseEntity<Void> deleteGroup(Long id) {
        groupManagementService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Group>> getAllGroups() {
        var groups = groupManagementService.getAllGroups().stream()
                .map(this::mapToApiGroup)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @Override
    public ResponseEntity<Group> getGroup(Long id) {
        return groupManagementService.getGroup(id)
                .map(group -> ResponseEntity.ok(mapToApiGroup(group)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Group> updateGroup(Long id, GroupRequest groupRequest) {
        var domainGroup = name.alexkrauss.scouts.domain.model.Group.builder()
                .id(id)
                .name(groupRequest.getName())
                .build();

        var updatedGroup = groupManagementService.updateGroup(domainGroup);
        return ResponseEntity.ok(mapToApiGroup(updatedGroup));
    }

    private Group mapToApiGroup(name.alexkrauss.scouts.domain.model.Group domainGroup) {
        var apiGroup = new Group();
        apiGroup.setId(domainGroup.getId());
        apiGroup.setName(domainGroup.getName());
        apiGroup.setVersion(domainGroup.getVersion());
        return apiGroup;
    }
}
