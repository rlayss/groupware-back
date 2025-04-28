package org.codenova.groupware.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupware.entity.Department;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.entity.Serial;
import org.codenova.groupware.repository.DepartmentRepository;
import org.codenova.groupware.repository.EmployeeRepository;
import org.codenova.groupware.repository.SerialRepository;
import org.codenova.groupware.request.AddEmployee;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SerialRepository serialRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<Employee> postEmployeeHandle(@ModelAttribute @Valid AddEmployee addEmployee,
                                                       BindingResult result) {
        if(result.hasErrors()) {
            return ResponseEntity.status(400).body(null);  // bad request : 서버가 클라이언트 오류를 감지해 요청을 처리할 수 없는 코드
        }
        // 1. 사원번호 생성, 부서 객체
        Optional<Serial> serial = serialRepository.findById(1); // JPA에서 id 로 찾는 걸 기본 제공해주는데, 결과가 Optional 객체가 나옴.
        // 정석적으로 사용한다면 serial.isPresent()  이런걸 확인해서 뽑아서 써야됨.
        Serial found = serial.get();    // 바로 뽑는 이유는 '100% 있는걸 알아서'
        Optional<Department> department = departmentRepository.findById(addEmployee.getDepartmentId());
        if(department.isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }
        // 2. 사원 객체 생성 및 저장
        Employee employee = Employee.builder()
                .id("GW-" +(found.getLastNumber()+1))
                .password(BCrypt.hashpw("0000", BCrypt.gensalt()))
                .name(addEmployee.getName())
                .active("N")
                .email(addEmployee.getEmail())
                .hireDate(addEmployee.getHireDate())
                .position(addEmployee.getPosition())
                .department(department.get())
                .build();
        employeeRepository.save(employee);
        // 시리얼 테이블의 last_number 를 업데이트 쳐줘야 함.
        found.setLastNumber(found.getLastNumber()+1);
        serialRepository.save(found);   // 수정할때 따른 메서드가 존재하지 않고

        return ResponseEntity.status(201).body(employee);   // created : 요청이 성공적으로 처리되었으며, 자원이 생성되었음을 나타내는 성공 상태 응답 코드
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeDetailHandle(@PathVariable String id) {

        Optional<Employee> employee = employeeRepository.findById(id);
        if(employee.isEmpty()) {
            return ResponseEntity.status(404).body(null);   // not found : 서버가 요청받은 리소스를 찾을 수 없다는 것을 의미
        }
        return ResponseEntity.status(200).body(employee.get());
    }

}
