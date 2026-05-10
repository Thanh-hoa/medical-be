package com.example.medical_be.service;

import com.example.medical_be.dto.res.FileUploadInfo;
import com.example.medical_be.dto.res.MediaUploadRes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaUploadCommonService {

    private final FileStorageService fileStorageService;

    public MediaUploadRes uploadFile(MultipartFile file) {
        FileUploadInfo fileUploadInfo = fileStorageService.store(file);
        return new MediaUploadRes(fileUploadInfo.path(), fileUploadInfo.fileName(), fileUploadInfo.extensionFile());
    }
}