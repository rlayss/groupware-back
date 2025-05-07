package org.codenova.groupware.repository;

import jakarta.persistence.Id;
import org.codenova.groupware.entity.Employee;
import org.codenova.groupware.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    public List<Note> findAllBySender(Employee sender);
}
