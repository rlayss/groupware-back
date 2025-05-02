package org.codenova.groupware.controller;

import lombok.RequiredArgsConstructor;
import org.codenova.groupware.entity.Board;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.repository.BoardRepository;
import org.codenova.groupware.repository.EmployeeRepository;
import org.codenova.groupware.request.AddBoard;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@CrossOrigin
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestAttribute String subject, @RequestBody AddBoard addBoard) throws Throwable {

        Optional<Employee> optionalEmployee = employeeRepository.findById(subject);

        Employee employee = optionalEmployee.orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        Board board = Board.builder()
                .writer(employee)
                .title(addBoard.getTitle())
                .content(addBoard.getContent())
                .wroteAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        boardRepository.save(board);

        return ResponseEntity.status(201).body(board);
    }

    @GetMapping
    public ResponseEntity<List<Board>> getBoards() {
        List<Board> boards = boardRepository.findAll(Sort.by("id").descending());
        return ResponseEntity.status(200).body(boards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        return ResponseEntity.status(200).body(board);
    }
}
