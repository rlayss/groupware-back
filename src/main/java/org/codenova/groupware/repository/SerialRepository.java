package org.codenova.groupware.repository;

import org.codenova.groupware.entity.Serial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SerialRepository extends JpaRepository<Serial, Integer> {
    Optional<Serial> findByRef(String employee);
}
