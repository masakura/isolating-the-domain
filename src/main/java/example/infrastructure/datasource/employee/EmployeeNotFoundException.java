package example.infrastructure.datasource.employee;

import example.domain.model.employee.EmployeeNumber;

public class EmployeeNotFoundException extends RuntimeException {

    EmployeeNumber employeeNumber;

    public EmployeeNotFoundException(EmployeeNumber employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public EmployeeNumber getEmployeeNumber() {
        return employeeNumber;
    }
}
