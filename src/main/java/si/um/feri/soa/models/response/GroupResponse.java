package si.um.feri.soa.models.response;

import si.um.feri.soa.models.request.GroupRequest;

public class GroupResponse extends GroupRequest {
    private String id;

    public GroupResponse() {
    }

    public GroupResponse(GroupRequest groupRequest) {
        super(groupRequest);
    }

    public GroupResponse(String id, GroupRequest groupRequest) {
        super(groupRequest);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
