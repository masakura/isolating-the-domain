package example.presentation.restcontroller;

import example.application.service.employee.EmployeeQueryService;
import example.application.service.employee.EmployeeRecordService;
import example.domain.model.employee.ContractingEmployees;
import example.domain.model.employee.Employee;
import example.domain.model.employee.EmployeeNumber;
import example.presentation.controller.employee.NewEmployee;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/employees")
@RestController
public class EmployeeAPI {

    EmployeeQueryService employeeQueryService;
    EmployeeRecordService employeeRecordService;

    public EmployeeAPI(EmployeeQueryService employeeQueryService, EmployeeRecordService employeeRecordService) {
        this.employeeQueryService = employeeQueryService;
        this.employeeRecordService = employeeRecordService;
    }

    @GetMapping
    ContractingEmployees employees() {
        return employeeQueryService.contractingEmployees();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    Employee register(@Valid @RequestBody NewEmployee newEmployee) {
        EmployeeNumber employeeNumber = employeeRecordService.prepareNewContract();
        employeeRecordService.registerName(employeeNumber, newEmployee.name());
        employeeRecordService.registerMailAddress(employeeNumber, newEmployee.mailAddress());
        employeeRecordService.registerPhoneNumber(employeeNumber, newEmployee.phoneNumber());
        employeeRecordService.inspireContract(employeeNumber);

        return employeeQueryService.choose(employeeNumber);
    }

    @GetMapping("{employeeNumber}")
    Employee employee(@PathVariable(value = "employeeNumber") EmployeeNumber employeeNumber) {
        return employeeQueryService.choose(employeeNumber);
    }
}
