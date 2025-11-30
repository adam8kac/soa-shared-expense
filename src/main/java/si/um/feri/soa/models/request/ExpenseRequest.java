package si.um.feri.soa.models.request;

import java.util.HashMap;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExpenseRequest {
    @PropertyName("description")
    private String description;
    @PropertyName("payments")
    private HashMap<String, Double> payments; // key == user id, value == placilo
    @PropertyName("totalAmount")
    private double totalAmount;
    @PropertyName("createdAt")
    private Timestamp createdAt;
    @PropertyName("updatedAt")
    private Timestamp updatedAt;

    public ExpenseRequest() {
        this.payments = new HashMap<>();
        this.totalAmount = 0;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ExpenseRequest(String description, HashMap<String, Double> payments) {
        this.description = description;
        this.payments = payments;
        this.totalAmount = 0;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ExpenseRequest(ExpenseRequest expenseRequest) {
        this.description = expenseRequest.description;
        this.payments = expenseRequest.payments;
        this.totalAmount = expenseRequest.totalAmount;
        this.createdAt = expenseRequest.createdAt;
        this.updatedAt = expenseRequest.updatedAt;
    }

}
