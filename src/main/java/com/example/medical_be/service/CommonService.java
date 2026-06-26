package com.example.medical_be.service;

import com.example.medical_be.dto.res.FileUploadInfo;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.MediaUploadRes;
import com.example.medical_be.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final FileStorageService fileStorageService;
    private final RoleRepository roleRepository;

    public MediaUploadRes uploadFile(MultipartFile file) {
        FileUploadInfo fileUploadInfo = fileStorageService.store(file);
        return new MediaUploadRes(fileUploadInfo.path(), fileUploadInfo.fileName(), fileUploadInfo.extensionFile());
    }

    public List<InfoAccountRes.Role> getRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .map(r -> new InfoAccountRes.Role(r.getId(), r.getName()))
                .toList();
    }
}