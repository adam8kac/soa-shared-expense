package si.um.feri.soa.services;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import si.um.feri.soa.models.request.GroupRequest;
import si.um.feri.soa.models.response.GroupResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class GroupService {
    private final Firestore firestore;
    private final String COLLECTION_NAME = "groups";
    private Logger logger = Logger.getLogger(getClass().getName());

    public GroupService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void addGroup(GroupRequest group) {
        try {
            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(group);
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add group to Firestore", e);
        }
    }

    public GroupResponse findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot doc = docRef.get().get();
            if (!doc.exists()) {
                throw new RuntimeException("Group not found");
            }
            return new GroupResponse(doc.getId(), doc.toObject(GroupRequest.class));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get group from Firestore", e);
        }
    }

    public List<GroupResponse> findAllGroupsOfUser(String userId) {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereArrayContains("groupMembers", userId)
                .get();

        try {
            QuerySnapshot querySnapshot = future.get();
            List<GroupResponse> groups = new ArrayList<GroupResponse>();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                GroupResponse groupResponse = new GroupResponse(doc.getId(), doc.toObject(GroupRequest.class));
                groups.add(groupResponse);
            }
            return groups;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get groups from Firestore", e);
        }
    }

    public List<String> getUserIds(String groupId) {
        if (groupId == null) {
            throw new Error("Group id should not be null");
        }

        ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION_NAME).document(groupId).get();

        try {
            DocumentSnapshot documentSnapshot = future.get();

            if (!documentSnapshot.exists()) {
                throw new Error("Document with id " + groupId + " does not exist");
            }

            GroupResponse groupResponse = new GroupResponse(documentSnapshot.getId(),
                    documentSnapshot.toObject(GroupRequest.class));
            List<String> userIds = groupResponse.getGroupMembers();

            return userIds;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get users of a group with that id", e);
        }
    }

    public boolean addGroupMember(String groupId, String memberId) {
        ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION_NAME).document(groupId).get();

        try {
            DocumentSnapshot doc = future.get();

            if (!doc.exists()) {
                throw new Exception("Group with that id was not found, " + groupId);
            }

            GroupResponse group = new GroupResponse(doc.getId(), doc.toObject(GroupRequest.class));
            if (group.getGroupMembers().contains(memberId)) {
                throw new Exception("User with id " + memberId + " already exists in group");
            }

            ArrayList<String> members = group.getGroupMembers();
            members.add(memberId.trim());
            group.setGroupMembers(members);

            ApiFuture<WriteResult> updateResult = firestore.collection(COLLECTION_NAME)
                    .document(groupId)
                    .update("groupMembers", members);
            updateResult.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get users of a group with that id", e);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return false;
    }

    public boolean changeGroupTitle(String id, String title) {
        ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION_NAME).document(id).get();

        try {
            DocumentSnapshot doc = future.get();

            if (!doc.exists()) {
                throw new Exception("Group with group id " + id + " does not exist");
            }

            ApiFuture<WriteResult> updateResult = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .update("groupTitle", title);
            updateResult.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get users of a group with that id", e);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return false;
    }

    public boolean deleteGroup(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new Exception("Group with group id " + id + " does not exist");
            }

            ApiFuture<WriteResult> apiFuture = docRef.delete();
            apiFuture.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get users of a group with that id", e);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return false;
    }

    public boolean removeMemberById(String groupId, String memberId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(groupId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new Exception("Group with that id " + groupId + " does not exists");
            }

            GroupResponse group = new GroupResponse(doc.getId(), doc.toObject(GroupResponse.class));
            List<String> newMembers = new ArrayList<>();

            if (!group.getGroupMembers().contains(memberId)) {
                logger.info("Member with id " + memberId + " does not exist in group with id " + groupId);
                return false;
            }

            for (String mId : group.getGroupMembers()) {
                if (!mId.equals(memberId)) {
                    newMembers.add(mId);
                }
            }

            ApiFuture<WriteResult> futureResult = docRef.update("groupMembers", newMembers);
            futureResult.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get users of a group with that id", e);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return false;
    }

    public List<String> getGroupMembers(String groupId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(groupId);
            DocumentSnapshot doc = docRef.get().get();
            GroupResponse group = new GroupResponse(doc.getId(), doc.toObject(GroupRequest.class));

            return group.getGroupMembers();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get members of a group with that id", e);
        }
    }
}
