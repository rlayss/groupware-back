package org.codenova.groupware.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.entity.Note;
import org.codenova.groupware.entity.NoteStatus;
import org.codenova.groupware.repository.EmployeeRepository;
import org.codenova.groupware.repository.NoteRepository;
import org.codenova.groupware.repository.NoteStatusRepository;
import org.codenova.groupware.request.AddNote;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/note")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class NoteController {
    private final EmployeeRepository employeeRepository;
    private final NoteRepository noteRepository;
    private final NoteStatusRepository noteStatusRepository;

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<?> createNote(@RequestAttribute String subject,
                                        @RequestBody @Valid AddNote addNote, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인자누락(내용 필수, 최소1명이상 수신사 설정 필수)");
        }
        // Note Save
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow( ()-> {

            return  new ResponseStatusException(HttpStatus.UNAUTHORIZED, "미인증 사원");
        });

        Note note= Note.builder().content(addNote.getContent())
                .sendAt(LocalDateTime.now()).isDelete(false).sender(subjectEmployee).build();
        noteRepository.save(note);  // 새 쪽지는 저장이 됬고,

        /*
        List<Employee> receivers = new ArrayList<>();
        for(String receiverId : addNote.getReceiverIds()) {
            receiver.add(employeeRepository.findById(receiverId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다.")
            ));
        }
         */
        /*
        List<Employee> receivers = addNote.getReceiverIds().stream().map((id)-> employeeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다.")
        )).toList();
        */

        List<Employee> receivers = employeeRepository.findAllById(addNote.getReceiverIds());



        /*
        List<NoteStatus> noteStatusList = new ArrayList<>();
        for(Employee e: receivers) {
            NoteStatus noteStatues = NoteStatus.builder()
                                  .note(note)
                                  .isRead(false)
                                  .isDelete(false)
                                  .receiver(e)
                                  .build();
            // noteStatusRepository.save(noteStatues);
            noteStatusList.add(noteStatus);
        }
        noteStatusRepository.saveAll(noteStatusList);
         */

        List<NoteStatus> noteStatus = receivers.stream().map((employee)-> {
            return NoteStatus.builder().note(note).isRead(false).isDelete(false).receiver(employee).build();
        }).toList();
        noteStatusRepository.saveAll(noteStatus);

        for(Employee receiver : receivers) {
            messagingTemplate.convertAndSend("/private/"+receiver.getId(), "새로운 쪽지를 수신하였습니다.");
        }


        return ResponseEntity.status(203).body(null);
    }


    @GetMapping("/inbox")
    public ResponseEntity<?> getReceiveNote(@RequestAttribute String subject) {
        // noteStatus 들중에 receiver 가 현재 로그인하고 있는 사용자로 되어있는 데이터만 가져와야함.
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"미인증 상태"));
//        NoteStatus example = NoteStatus.builder().receiver(subjectEmployee).build();
//        List<NoteStatus> noteStatusList =noteStatusRepository.findAll(Example.of(example));

        List<NoteStatus> noteStatusList = noteStatusRepository.findAllByReceiver(subjectEmployee);
        return ResponseEntity.status(200).body(noteStatusList);

    }

    @GetMapping("/outbox")
    public ResponseEntity<?> getSendNote(@RequestAttribute String subject) {
        // note 리포지토리에서 이 요청을 보낸 사용자가 쓴 note 를 가지고 와야 한다.
        Employee subjectEmployee = employeeRepository.findById(subject)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        /*
            Optional<Employee> optionalSubject = employeeRepository.findById(subject);
            if(optionalSubject.isEmpty() ) {
               throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
            }
            Employee subjectEmployee = optionalSubject.get();
         */
        List<Note> sendNotes = noteRepository.findAllBySender(subjectEmployee);
        List<NoteStatus> sendNoteStatus =
                noteStatusRepository.findAllByNoteIn(sendNotes);

        return ResponseEntity.status(200).body(sendNoteStatus);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> putStatusHandle(@RequestAttribute String subject, @PathVariable Long id) {
        Optional<NoteStatus> optionalNoteStatus =noteStatusRepository.findById(id);
        if(optionalNoteStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "잘못된 id 값이 전달.");
        }
        NoteStatus noteStatus = optionalNoteStatus.get();
        // 리시버가 요청을 사용자가 아니면,,? 이건 권한 없음
        if(!noteStatus.getReceiver().getId().equals(subject)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자신이 받은 쪽지만 상태 변경 가능.");
        }
        if(!noteStatus.getIsRead()) {
            noteStatus.setIsRead(true);
            noteStatus.setReadAt(LocalDateTime.now());
            noteStatusRepository.save(noteStatus);
            messagingTemplate.convertAndSend("/private/"+noteStatus.getNote().getSender().getId(), subject+"가 당신이 보낸 쪽지를 확인하였습니다." );
        }

        return ResponseEntity.status(200).body(noteStatus);
    }

}
