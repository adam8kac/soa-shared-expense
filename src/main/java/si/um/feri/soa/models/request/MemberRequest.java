package si.um.feri.soa.models.request;

import lombok.ToString;

@ToString
public class MemberRequest {
    private String memberId;

    public MemberRequest() {
    }

    public MemberRequest(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

}
