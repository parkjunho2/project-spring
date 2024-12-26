package com.kh.topgunFinal.restcontroller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.topgunFinal.configuration.CustomCertProperties;
import com.kh.topgunFinal.dao.CertDao;
import com.kh.topgunFinal.dto.CertDto;
import com.kh.topgunFinal.service.EmailService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.mail.MessagingException;


@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"})
@RestController
@RequestMapping("/cert")
public class CertRestController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private CertDao certDao;

	@Autowired
	private CustomCertProperties customCertProperties;

	// 사용자가 요구하는 이메일로 인증메일을 발송하는 기능
	@PostMapping("/send")
	public void send(@RequestParam String certEmail,@RequestParam String certId ) throws MessagingException, IOException {
		emailService.sendResetPw(certId, certEmail);
	}

	// 사용자가 입력한 인증번호가 유효한지를 판정하는 기능
	@PostMapping("/check")
	public boolean check(@RequestBody CertDto certDto) {
		boolean result = certDao.check(certDto, customCertProperties.getExpire());
		if (result) {// 인증 성공 시
			certDao.delete(certDto.getCertEmail());// 인증번호 삭제
		}
		return result;
	}

}
