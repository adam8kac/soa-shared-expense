package si.um.feri.soa.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import si.um.feri.soa.models.request.ExpenseRequest;

public class ExpenseResponse extends ExpenseRequest {
    private String id;

    @JsonProperty("createdAt")
    private String createdAtString;

    @JsonProperty("updatedAt")
    private String updatedAtString;

    public ExpenseResponse() {
        super();
    }

    public ExpenseResponse(ExpenseRequest expenseRequest) {
        super(expenseRequest);
    }

    public ExpenseResponse(String id, ExpenseRequest expenseRequest) {
        super(expenseRequest);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAtString() {
        return createdAtString;
    }

    public void setCreatedAtString(String createdAtString) {
        this.createdAtString = createdAtString;
    }

    public String getUpdatedAtString() {
        return updatedAtString;
    }

    public void setUpdatedAtString(String updatedAtString) {
        this.updatedAtString = updatedAtString;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public com.google.cloud.Timestamp getCreatedAt() {
        return super.getCreatedAt();
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public com.google.cloud.Timestamp getUpdatedAt() {
        return super.getUpdatedAt();
    }
}
