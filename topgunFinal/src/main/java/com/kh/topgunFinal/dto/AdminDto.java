package com.kh.topgunFinal.dto;

import lombok.Data;

@Data
public class AdminDto{
    private String userId; // user_id //  admin user ID (Foreign Key referencing User)
    private String adminDepartment; // 관리자 부서 // admin_department
    private String adminAccessLevel; // 관리자 접근 수준 // admin_access_level
    private String flightId; // 관련 항공편 ID (Foreign Key referencing Flight)3차시도
}

