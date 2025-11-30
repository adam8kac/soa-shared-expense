package si.um.feri.soa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import si.um.feri.soa.models.request.ExpenseRequest;
import si.um.feri.soa.models.response.ExpenseResponse;

@Service
public class ExpenseService {
    private final Firestore firestore;
    private final String COLLECTION_NAME = "groups";
    private final String SUBCOLLECTION_NAME = "expenses";
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    private GroupService groupService;

    public ExpenseService(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean addExpenseToAGroup(String groupId, ExpenseRequest expense) {
        if (groupId == null) {
            logger.warning("Group id is null - addExpenseToAGroup");
            return false;
        }

        double totalAmount = 0;
        for (var x : expense.getPayments().entrySet()) {
            totalAmount += x.getValue();
        }
        expense.setTotalAmount(totalAmount);

        try {
            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME)
                    .document(groupId)
                    .collection(SUBCOLLECTION_NAME)
                    .add(expense);
            future.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add group to Firestore", e);
        }
    }

    public List<ExpenseResponse> getAllExpensesOfAGroup(String groupId) {
        if (groupId == null) {
            throw new Error("Group id should not be null");
        }

        try {
            ApiFuture<QuerySnapshot> queryFuture = firestore.collection(COLLECTION_NAME).document(groupId)
                    .collection(SUBCOLLECTION_NAME).get();
            QuerySnapshot querySnapshot = queryFuture.get();
            List<ExpenseResponse> expenseList = new ArrayList<>();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                ExpenseRequest expenseRequest;
                try {
                    expenseRequest = doc.toObject(ExpenseRequest.class);
                    if (expenseRequest != null) {
                        if (doc.contains("createdAt")) {
                            expenseRequest.setCreatedAt(doc.getTimestamp("createdAt"));
                        }
                        if (doc.contains("updatedAt")) {
                            expenseRequest.setUpdatedAt(doc.getTimestamp("updatedAt"));
                        }
                    }
                } catch (RuntimeException e) {
                    expenseRequest = new ExpenseRequest();
                    expenseRequest.setDescription(doc.getString("description"));

                    @SuppressWarnings("unchecked")
                    java.util.HashMap<String, Double> payments = (java.util.HashMap<String, Double>) doc
                            .get("payments");
                    expenseRequest.setPayments(payments != null ? payments : new java.util.HashMap<>());

                    expenseRequest
                            .setTotalAmount(doc.getDouble("totalAmount") != null ? doc.getDouble("totalAmount") : 0.0);

                    if (doc.contains("createdAt")) {
                        expenseRequest.setCreatedAt(doc.getTimestamp("createdAt"));
                    }
                    if (doc.contains("updatedAt")) {
                        expenseRequest.setUpdatedAt(doc.getTimestamp("updatedAt"));
                    }
                }

                ExpenseResponse res = new ExpenseResponse(doc.getId(), expenseRequest);

                if (expenseRequest.getCreatedAt() != null) {
                    res.setCreatedAtString(expenseRequest.getCreatedAt().toDate().toInstant().toString());
                }
                if (expenseRequest.getUpdatedAt() != null) {
                    res.setUpdatedAtString(expenseRequest.getUpdatedAt().toDate().toInstant().toString());
                }

                expenseList.add(res);
            }
            return expenseList;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get expenses from Firestore", e);
        }
    }

    public boolean deleteExpense(String groupId, String expenseId) {
        if (groupId == null) {
            throw new Error("Group id should not be null");
        }
        if (expenseId == null) {
            throw new Error("Expense id should not be null");
        }

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(groupId)
                    .collection(SUBCOLLECTION_NAME).document(expenseId);
            DocumentSnapshot doc = docRef.get().get();
            if (!doc.exists()) {
                logger.warning("Expense with id " + expenseId + " does not exist");
                return false;
            }

            ApiFuture<WriteResult> apiFuture = docRef.delete();
            apiFuture.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get expense of a group with that id", e);
        }
    }

    public boolean updateExpense(String groupId, String expenseId, ExpenseRequest expense) {
        if (groupId == null) {
            logger.warning("Group id is null - updateExpense");
            return false;
        }
        if (expenseId == null) {
            logger.warning("Expense id is null - updateExpense");
            return false;
        }
        if (expense == null) {
            logger.warning("Expense is null - updateExpense");
            return false;
        }

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(groupId)
                    .collection(SUBCOLLECTION_NAME).document(expenseId);
            DocumentSnapshot doc = docRef.get().get();
            if (!doc.exists()) {
                logger.warning("Expense with id " + expenseId + " does not exist");
                return false;
            }

            double totalAmount = 0;
            for (var x : expense.getPayments().entrySet()) {
                totalAmount += x.getValue();
            }
            expense.setTotalAmount(totalAmount);

            if (doc.contains("createdAt")) {
                expense.setCreatedAt(doc.getTimestamp("createdAt"));
            } else {
                expense.setCreatedAt(com.google.cloud.Timestamp.now());
            }
            expense.setUpdatedAt(com.google.cloud.Timestamp.now());

            ApiFuture<WriteResult> apiFuture = docRef.set(expense);
            apiFuture.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update expense in Firestore", e);
        }
    }

    public Double getGroupsTotalSpendings(String groupId) {
        List<ExpenseResponse> expenses = getAllExpensesOfAGroup(groupId);
        double totalAmount = 0;

        for (ExpenseResponse expense : expenses) {
            totalAmount += expense.getTotalAmount();
        }
        return totalAmount;
    }

    public HashMap<String, Double> getEachMemberSpendings(String groupId) {
        List<ExpenseResponse> expenses = getAllExpensesOfAGroup(groupId);
        List<String> groupMembers = groupService.getGroupMembers(groupId);
        HashMap<String, Double> spendingsByMember = new HashMap<>();

        for (String uid : groupMembers) {
            if (!spendingsByMember.containsKey(uid)) {
                spendingsByMember.put(uid, 0.0);
            }
        }

        for (ExpenseResponse expense : expenses) {
            for (Entry<String, Double> payment : expense.getPayments().entrySet()) {
                String memberId = payment.getKey();
                Double paymentAmount = Math.round(payment.getValue() * 100.0) / 100.0;

                if (spendingsByMember.containsKey(memberId)) {
                    double newValue = spendingsByMember.get(memberId) + paymentAmount;
                    spendingsByMember.put(memberId, Math.round(newValue * 100.0) / 100.0);
                } else {
                    spendingsByMember.put(memberId, paymentAmount);
                }
            }
        }
        return spendingsByMember;
    }

    public HashMap<String, List<HashMap<String, Double>>> splitDebt(String groupId) {
        HashMap<String, Double> totalSpentByMember = getEachMemberSpendings(groupId);
        double totalSpent = getGroupsTotalSpendings(groupId);
        double toPayPerMember = Math.round((totalSpent / totalSpentByMember.size()) * 100) / 100;
        HashMap<String, Double> isOwed = new HashMap<>();
        HashMap<String, Double> owes = new HashMap<>();
        HashMap<String, List<HashMap<String, Double>>> splitMoney = new HashMap<>();

        for (Entry<String, Double> object : totalSpentByMember.entrySet()) {
            String key = object.getKey();
            Double spendings = object.getValue();
            double amountToPay = Math.round((toPayPerMember - spendings) * 100) / 100 + 1;
            if (amountToPay > 0) {
                owes.put(key, amountToPay);
            } else if (amountToPay < 0) {
                isOwed.put(key, -amountToPay);
            }
        }

        for (Entry<String, Double> memberThatGetsMoney : isOwed.entrySet()) {
            double amountToGet = memberThatGetsMoney.getValue();
            while (amountToGet != 0) {
                for (Entry<String, Double> memberThatOwesMoney : owes.entrySet()) {

                    if (amountToGet == 0.0) {
                        break;
                    }

                    double amountStillOwed = memberThatOwesMoney.getValue();
                    HashMap<String, Double> payment = new HashMap<>();

                    if (!splitMoney.containsKey(memberThatGetsMoney.getKey())) {
                        splitMoney.put(memberThatGetsMoney.getKey(), new ArrayList<>());
                    }

                    if (amountToGet >= amountStillOwed) {
                        if (amountStillOwed > 0.0) {
                            payment.put(memberThatOwesMoney.getKey(), amountStillOwed);
                            if (!splitMoney.containsKey(memberThatGetsMoney.getKey())) {
                                splitMoney.put(memberThatGetsMoney.getKey(), new ArrayList<>());
                            }
                            splitMoney.get(memberThatGetsMoney.getKey()).add(payment);
                        }
                        memberThatOwesMoney.setValue(0.0);
                        memberThatGetsMoney.setValue(amountToGet - amountStillOwed);
                        amountToGet -= amountStillOwed;
                    } else {
                        if (amountToGet > 0.0) {
                            double moneyLeft = amountStillOwed - amountToGet;
                            payment.put(memberThatOwesMoney.getKey(), amountToGet);
                            if (!splitMoney.containsKey(memberThatGetsMoney.getKey())) {
                                splitMoney.put(memberThatGetsMoney.getKey(), new ArrayList<>());
                            }
                            splitMoney.get(memberThatGetsMoney.getKey()).add(payment);
                            memberThatOwesMoney.setValue(moneyLeft);
                        } else {
                            memberThatOwesMoney.setValue(amountStillOwed);
                        }
                        memberThatGetsMoney.setValue(0.0);
                        amountToGet = 0.0;
                    }
                }
                break;
            }
        }

        return splitMoney;
    }

}
