package com.kh.topgunFinal.vo;

import java.util.List;

import com.kh.topgunFinal.dto.PaymentDetailDto;
import com.kh.topgunFinal.dto.PaymentDto;
import com.kh.topgunFinal.vo.pay.PayOrderResponseVO;

import lombok.Data;

@Data
public class PaymentInfoVO {
	private PaymentDto paymentDto;
	private List<PaymentDetailDto> paymentDetailList;
	private PayOrderResponseVO responseVO;
	private List<TimerVO> timerVO;
}
