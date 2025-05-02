package org.codenova.groupware.repository;

import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.entity.NoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteStatusRepository extends JpaRepository<NoteStatus, Long> {

    public List<NoteStatus> findAllByReceiver(Employee employee);
    public List<NoteStatus> findAllByReceiverAndIsRead(Employee employee, Boolean isRead);
}


