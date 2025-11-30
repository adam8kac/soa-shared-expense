package si.um.feri.soa.models.request;

import java.util.ArrayList;

import com.google.cloud.firestore.annotation.PropertyName;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class GroupRequest {
    @PropertyName("groupTitle")
    private String groupTitle;

    @PropertyName("groupMembers")
    private ArrayList<String> groupMembers;

    @PropertyName("groupTitle")
    public String getGroupTitle() {
        return groupTitle;
    }

    @PropertyName("groupTitle")
    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    @PropertyName("groupMembers")
    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    @PropertyName("groupMembers")
    public void setGroupMembers(ArrayList<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public GroupRequest() {
    }

    public GroupRequest(GroupRequest groupRequest) {
        this.groupTitle = groupRequest.getGroupTitle();
        this.groupMembers = groupRequest.getGroupMembers();
    }
}
