package org.codenova.groupware.controller;

import lombok.RequiredArgsConstructor;
import org.codenova.groupware.entity.Board;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.repository.BoardRepository;
import org.codenova.groupware.repository.EmployeeRepository;
import org.codenova.groupware.request.AddBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestAttribute String subject, @RequestBody AddBoard addBoard) throws Throwable {

        Optional<Employee> optionalEmployee = employeeRepository.findById(subject);

        Employee employee = optionalEmployee.orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        Board board = Board.builder().writer(employee).title(addBoard.getTitle()).content(addBoard.getContent()).wroteAt(LocalDateTime.now()).viewCount(0).build();

        boardRepository.save(board);
        messagingTemplate.convertAndSend("/public", "새글이 등록되었습니다.");

        return ResponseEntity.status(201).body(board);
    }

    @GetMapping
    public ResponseEntity<?> getBoards(@RequestParam(name = "p") Optional<Integer> p) {
//        List<Board> boards = boardRepository.findAll(Sort.by("id").descending());
        int pageNumber = p.orElse(1);
        pageNumber = Math.max(pageNumber, 1);

        Page<Board> boards = boardRepository.findAll(PageRequest.of(pageNumber - 1, 5));    // 첫번째 인자가 페이지인덱스(0,, 두번째인자가 몇개씩 페이징 처리할껀지


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
