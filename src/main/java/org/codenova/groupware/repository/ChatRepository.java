package org.codenova.groupware.repository;

import org.codenova.groupware.entity.Chat;
import org.codenova.groupware.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    public List<Chat> findAllByDepartmentOrderById(Department department);
}

