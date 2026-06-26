package com.example.medical_be.service.configuration;

import java.util.List;

import com.example.medical_be.dto.res.MenuItemRes;

public interface IMenuPermissionService {
    List<MenuItemRes> menuPermissionOfUser();
}
