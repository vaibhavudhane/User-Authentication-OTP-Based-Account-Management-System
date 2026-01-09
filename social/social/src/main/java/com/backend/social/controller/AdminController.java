package com.backend.social.controller;

import com.backend.social.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService)
    {
        this.adminService=adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam String identifier) {
        adminService.deleteUserByIdentifier(identifier);
        return ResponseEntity.ok( "User deleted successfully");
    }
}


