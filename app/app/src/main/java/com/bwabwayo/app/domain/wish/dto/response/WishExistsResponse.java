package com.bwabwayo.app.domain.wish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishExistsResponse {
    private boolean exists;

    public static WishExistsResponse from(boolean exists) {
        return new WishExistsResponse(exists);
    }
}
