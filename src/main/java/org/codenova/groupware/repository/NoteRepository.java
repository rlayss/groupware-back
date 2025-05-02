package org.codenova.groupware.repository;

import jakarta.persistence.Id;
import org.codenova.groupware.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {


}

