package com.giveitup.giveitup_be.projection;


public interface DashboardSummaryProjection {

    Long getTotalPost();

    Long getDonateCount();

    Double getTotalDonated();

    Long getTotalView();

    Long getTotalLike();

    Long getTotalComment();
}
