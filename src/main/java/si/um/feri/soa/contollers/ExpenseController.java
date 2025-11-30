package si.um.feri.soa.contollers;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import si.um.feri.soa.models.request.ExpenseRequest;
import si.um.feri.soa.models.response.ExpenseResponse;
import si.um.feri.soa.services.ExpenseService;

@RestController
@RequestMapping("/group-expenses")
public class ExpenseController {
    @Autowired
    private ExpenseService service;

    @PostMapping("/add/{groupId}")
    public ResponseEntity<String> addGroupExpense(@PathVariable String groupId, @RequestBody ExpenseRequest expense) {
        if (groupId == null) {
            return ResponseEntity.badRequest().body("Group id is null");
        }
        if (expense == null) {
            return ResponseEntity.badRequest().body("Expense body can not be null");
        }
        if (!service.addExpenseToAGroup(groupId, expense)) {
            return ResponseEntity.badRequest().body("could not add expnese to a group with id " + groupId);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/get-all/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getAllExpensesOfAGroup(@PathVariable String groupId) {
        if (groupId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<ExpenseResponse> expenses = service.getAllExpensesOfAGroup(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @DeleteMapping("/delete/{groupId}/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable String groupId, @PathVariable String expenseId) {
        if (groupId == null) {
            return ResponseEntity.badRequest().body("Group id is null");
        }
        if (expenseId == null) {
            return ResponseEntity.badRequest().body("Expense id is null");
        }
        if (!service.deleteExpense(groupId, expenseId)) {
            return ResponseEntity.badRequest()
                    .body("could not delete expense with id " + expenseId + " from group with id " + groupId);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/update/{groupId}/{expenseId}")
    public ResponseEntity<String> updateExpense(@PathVariable String groupId, @PathVariable String expenseId,
            @RequestBody ExpenseRequest expense) {
        if (groupId == null) {
            return ResponseEntity.badRequest().body("Group id is null");
        }
        if (expenseId == null) {
            return ResponseEntity.badRequest().body("Expense id is null");
        }
        if (expense == null) {
            return ResponseEntity.badRequest().body("Expense body can not be null");
        }
        if (!service.updateExpense(groupId, expenseId, expense)) {
            return ResponseEntity.badRequest()
                    .body("could not update expense with id " + expenseId + " from group with id " + groupId);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/total/{groupId}")
    public ResponseEntity<Double> getTotalSpendings(@PathVariable String groupId) {
        if (groupId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.getGroupsTotalSpendings(groupId));
    }

    @GetMapping("/each-spent/{groupId}")
    public ResponseEntity<HashMap<String, Double>> eachMemberSpendings(@PathVariable String groupId) {
        if (groupId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.getEachMemberSpendings(groupId));
    }

    @GetMapping("split/{groupId}")
    public ResponseEntity<HashMap<String, List<HashMap<String, Double>>>> splitBetweenMembers(
            @PathVariable String groupId) {
        if (groupId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.splitDebt(groupId));
    }
}