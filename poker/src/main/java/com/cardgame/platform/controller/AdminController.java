package com.cardgame.platform.controller;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/tables") public List<AdminTableResponse> tables() { return adminService.tables(); }
    @GetMapping("/tables/{tableId}") public AdminTableResponse table(@PathVariable Long tableId) { return adminService.table(tableId); }
    @GetMapping("/coin-transactions") public List<AdminCoinTransactionResponse> history() { return adminService.history(); }
    @PostMapping("/coins/player") public List<AdminCoinTransactionResponse> distributeToPlayer(@Valid @RequestBody AdminCoinDistributionRequest request, Authentication authentication) { return adminService.distributeToPlayer(authentication.getName(), request); }
    @PostMapping("/tables/{tableId}/coins") public List<AdminCoinTransactionResponse> distributeToTable(@PathVariable Long tableId, @Valid @RequestBody AdminTableCoinDistributionRequest request, Authentication authentication) { return adminService.distributeToTable(authentication.getName(), tableId, request); }
}
