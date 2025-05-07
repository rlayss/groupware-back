package org.codenova.groupware.repository;

import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.entity.Note;
import org.codenova.groupware.entity.NoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
    spring jpa custom method
    https://docs.spring.io/spring-data/jpa/reference/repositories/custom-implementations.html
 */
public interface NoteStatusRepository extends JpaRepository<NoteStatus, Long> {

    public List<NoteStatus> findAllByReceiver(Employee employee);

    public List<NoteStatus> findAllByReceiverAndIsRead(Employee employee, Boolean isRead);


    public List<NoteStatus> findAllByNoteIn(List<Note> notes);
}
