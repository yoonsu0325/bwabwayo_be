package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // 추가 메서드 예시 (필요할 때 사용)
    Account findByUser_Id(String userId);
}

