package com.monad.talep.entity;

/**
 * Musteri (CUSTOMER) abonelik plani. Odeme ekrani YOK — yukseltme/dusurme
 * demo amacli anlik yapilir (admin ya da musterinin kendisi tarafindan).
 * Plan sadece acik (NEW/UNDER_REVIEW/PRIORITIZED) talep adedini sinirlar.
 */
public enum PlanType {
    FREE(3, "Free"),
    PRO(15, "Pro"),
    PRO_PLUS(Integer.MAX_VALUE, "Pro Plus");

    private final int maxOpenRequests;
    private final String displayName;

    PlanType(int maxOpenRequests, String displayName) {
        this.maxOpenRequests = maxOpenRequests;
        this.displayName = displayName;
    }

    public int getMaxOpenRequests() {
        return maxOpenRequests;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUnlimited() {
        return maxOpenRequests == Integer.MAX_VALUE;
    }
}
