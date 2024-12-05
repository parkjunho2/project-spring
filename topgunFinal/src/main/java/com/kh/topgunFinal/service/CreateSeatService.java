package com.kh.topgunFinal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.topgunFinal.dto.SeatsDto;

@Service
public class CreateSeatService {

	public List<SeatsDto> createList(int seatRow, int seatCol, int flightId) {

		// seatList를 ArrayList로 초기화
		List<SeatsDto> seatList = new ArrayList<>();
		for (int row = 1; row <= seatRow; row++) {
			for (int col = 1; col <= seatCol; col++) {
				SeatsDto seat = new SeatsDto();

				seat.setFlightId(flightId);
				seat.setSeatsNo((row - 1) * seatCol + col); // 좌석 번호 계산 (고유 번호)

				// row 값을 알파벳으로 변환 ('A'는 65의 ASCII 코드)
				char rowLetter = (char) ('A' + row - 1);
				String seatNumber = rowLetter + String.valueOf(col);

				seat.setSeatsNumber(seatNumber); // 좌석 번호 설정

				// A열이나 B열일 때 비즈니스, 나머지는 이코노미
				String seatRank = (rowLetter == 'A' || rowLetter == 'B') ? "비즈니스" : "이코노미";
				seat.setSeatsRank(seatRank); // 좌석 등급 설정

				// 좌석 가격 설정
				if ("비즈니스".equals(seatRank)) {
					seat.setSeatsPrice(80000); // 비즈니스 80,000원
				} else {
					seat.setSeatsPrice(0); // 이코노미 0원
				}
				// seatList에 추가
				seatList.add(seat);
			}
		}
		return seatList;
	}
}
