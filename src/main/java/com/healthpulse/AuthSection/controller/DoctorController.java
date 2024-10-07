package com.healthpulse.AuthSection.controller;

import java.util.List;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.healthpulse.AuthSection.payloads.ApiResponse;
import com.healthpulse.AuthSection.payloads.DoctorInfoDto;
import com.healthpulse.AuthSection.payloads.UserDto;
import com.healthpulse.AuthSection.service.FileService;
import com.healthpulse.AuthSection.service.UserService;

import io.jsonwebtoken.io.IOException;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private UserService userService;
    
    @Autowired
	private FileService fileService;
	
	@Value("${project.image}")
	private String path;

    @PutMapping("/update-info/{userId}")
    public ResponseEntity<UserDto> updateDoctorInfo(
        @RequestParam(value = "specialization", required = false) String specialization,
        @RequestParam(value = "degrees", required = false) String degrees,
        @RequestParam(value = "certificates", required = false) String certificates,
        @RequestParam(value = "cv", required = false) MultipartFile cv,
        @RequestParam(value = "certificateOfRegistration", required = false) MultipartFile certificateOfRegistration,
        @RequestParam(value = "experience", required = false) String experience,
        @PathVariable("userId") Integer userId
    ) throws IOException, java.io.IOException {

        UserDto userDto = userService.getUserById(userId);

        if (userDto.getDoctorInfo() != null) {
            DoctorInfoDto doctorInfoDto = userDto.getDoctorInfo();
            
            // Update fields
            if (specialization != null) doctorInfoDto.setSpecialization(specialization);
            if (degrees != null) doctorInfoDto.setDegrees(degrees);
            if (certificates != null) doctorInfoDto.setCertificates(certificates);
            if (cv != null && !cv.isEmpty()) {
                String cvFileName = fileService.uploadImage(path, cv);
                doctorInfoDto.setCV(cvFileName);
            }
            if (certificateOfRegistration != null && !certificateOfRegistration.isEmpty()) {
                String certificateFileName = fileService.uploadImage(path, certificateOfRegistration);
                doctorInfoDto.setCertificateOfRegistration(certificateFileName);
            }
            if (experience != null) doctorInfoDto.setExperience(experience);

            // Update DoctorInfo
            userDto.setDoctorInfo(doctorInfoDto);
            userDto = userService.updateUser(userDto, userId);
        }

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve/{doctorId}")
    public ResponseEntity<UserDto> approveDoctor(@PathVariable ("doctorId") int doctorId) throws MessagingException {
        UserDto updatedUser = userService.approveDoctor(doctorId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reject/{doctorId}")
    public ResponseEntity<UserDto> rejectDoctor(@PathVariable ("doctorId") int doctorId) {
        UserDto updatedUser = userService.rejectDoctor(doctorId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<UserDto>> getPendingDoctorApprovals() {
        List<UserDto> pendingApprovals = userService.getPendingDoctorApprovals();
        return new ResponseEntity<>(pendingApprovals, HttpStatus.OK);
    }
}
