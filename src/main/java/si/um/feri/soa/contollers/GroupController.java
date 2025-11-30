package si.um.feri.soa.contollers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import si.um.feri.soa.models.request.MemberRequest;
import si.um.feri.soa.models.request.GroupRequest;
import si.um.feri.soa.models.request.GroupTitleRequest;
import si.um.feri.soa.models.response.GroupResponse;
import si.um.feri.soa.services.GroupService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    private GroupService service;

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody GroupRequest group) {

        if (group == null) {
            return ResponseEntity.badRequest().body("Group cannot be null");
        }

        service.addGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body("Group created successfully");
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<Group> getGroupById(@PathVariable String id) {
    // Group group = service.findById(id);
    // }

    @GetMapping("/user-groups/{userId}")
    public ResponseEntity<List<GroupResponse>> getAllGroupsOfUser(@PathVariable String userId) {
        List<GroupResponse> groups = service.findAllGroupsOfUser(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> findGroupById(@PathVariable String id) {
        GroupResponse group = service.findById(id);

        if (group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(group);
    }

    @GetMapping("/group-members/{id}")
    public ResponseEntity<List<String>> getUserIdsFromGroup(@PathVariable String id) {
        List<String> userIds = service.getUserIds(id);

        if (userIds == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(userIds);
    }

    @PostMapping("/add-member/{id}")
    public ResponseEntity<String> addGroupMember(@PathVariable String id, @RequestBody MemberRequest memberId) {
        if (!service.addGroupMember(id, memberId.getMemberId())) {
            return ResponseEntity.badRequest()
                    .body("Member with id " + memberId.getMemberId() + " could not be added into group with id " + id);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update-title/{id}")
    public ResponseEntity<String> changeGroupTitle(@PathVariable String id, @RequestBody GroupTitleRequest title) {
        if (!service.changeGroupTitle(id, title.getTitle())) {
            return ResponseEntity.badRequest()
                    .body("Could not change the tilte of a group " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/delete-group/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable String id) {
        if (!service.deleteGroup(id)) {
            return ResponseEntity.badRequest()
                    .body("Could not delete a group with id " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/remove-member/{id}")
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable String id, @RequestBody MemberRequest memberId) {
        if (!service.removeMemberById(id, memberId.getMemberId())) {
            return ResponseEntity.badRequest()
                    .body("Could not remove a member with id " + memberId.getMemberId() + " from a group with id "
                            + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body("deleted member " + memberId.getMemberId());
    }

}
