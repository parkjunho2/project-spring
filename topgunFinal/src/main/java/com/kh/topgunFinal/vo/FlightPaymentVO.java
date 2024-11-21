package com.kh.topgunFinal.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class FlightPaymentVO {
    private String flightNumber;      // 항공편 번호
    private Long totalPayment;        // 총 매출
    private String airlineName;       // 항공사 이름
    private Date departureTime;       // 출발 시간
    private String month;            
}