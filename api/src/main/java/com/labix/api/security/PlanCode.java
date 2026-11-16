package com.labix.api.security;

public enum PlanCode {
    WEB, MOB_COLLECT, MOB_FULL;

    public boolean includes(PlanFeature feature) {
        return switch (feature) {
            case MOBILE_ACCESS -> this != WEB;
            case MOBILE_COLLECTION -> this != WEB;
            case MOBILE_FULL_ACCESS -> this == MOB_FULL;
        };
    }
}
