package com.kh.topgunFinal.vo;

import lombok.Data;

@Data
public class SeatsCountVO {
    private int flightId;
    private int totalSeatsNo;
    private int unusedSeatsCount;
}
