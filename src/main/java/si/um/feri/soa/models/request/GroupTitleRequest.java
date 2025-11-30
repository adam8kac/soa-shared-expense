package si.um.feri.soa.models.request;

import lombok.ToString;

@ToString
public class GroupTitleRequest {
    private String title;

    public GroupTitleRequest() {
    }

    public GroupTitleRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
