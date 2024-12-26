 package com.kh.topgunFinal.vo.pay;

import java.util.List;

import com.kh.topgunFinal.dto.SeatsDto;
import com.kh.topgunFinal.vo.SeatsQtyVO;

import lombok.Data;

@Data
public class PayApproveRequestVO {
	private String partnerOrderId;//중복 방지
	private String partnerUserId;
	private String tid;
	private String pgToken;
	private List<SeatsQtyVO> seatsList;
}
