package org.codenova.groupware.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupware.entity.Chat;
import org.codenova.groupware.entity.Department;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.repository.ChatRepository;
import org.codenova.groupware.repository.DepartmentRepository;
import org.codenova.groupware.repository.EmployeeRepository;
import org.codenova.groupware.request.AddChat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{departmentId}")
    public ResponseEntity<?> postChatHandle(@RequestAttribute String subject,
                                            @RequestBody AddChat addChat,
                                            @PathVariable Integer departmentId) {
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow();
        Department department =
                departmentRepository.findById(departmentId).orElseThrow();

        Chat chat = Chat.builder().talker(subjectEmployee)
                .message(addChat.getMessage()).department(department).build();

        chatRepository.save(chat);
        log.info("새 채팅 등록 요청 처리 완료");

        messagingTemplate.convertAndSend("/chat-department/"+departmentId, "newChat");
        return ResponseEntity.status(201).body(chat);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getChatHandle(@PathVariable Integer departmentId) {
        Department department =
                departmentRepository.findById(departmentId).orElseThrow();

        List<Chat> chatList = chatRepository.findAllByDepartmentOrderById(department);

        return ResponseEntity.status(200).body(chatList);
    }
}
