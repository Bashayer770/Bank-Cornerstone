package com.bank.shop

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/store/user")
class StoreController(
    private val storeService: StoreService
) {

    @GetMapping("/{userId}")
    fun getItemsForUser(@PathVariable userId: Long): List<StoreItem> {
        return storeService.getStoreItemsForUser(userId)
    }

    @PostMapping("/{userId}")
    fun purchaseItem(
        @PathVariable userId: Long,
        @RequestBody request: PurchaseRequest
    ): PurchaseResponse {
        return storeService.purchaseItem(userId, request.itemId)
    }
}
