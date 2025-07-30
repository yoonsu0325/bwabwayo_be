package com.bwabwayo.app.domain.product.event;

import java.util.List;

public record ProductDeletedEvent(List<String> imageKeys) {}
