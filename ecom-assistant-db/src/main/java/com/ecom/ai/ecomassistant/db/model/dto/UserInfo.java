package com.ecom.ai.ecomassistant.db.model.dto;

import com.ecom.ai.ecomassistant.common.UserStatus;
import lombok.Data;

@Data
public class UserInfo {
    private String id;
    private String name;
    private String email;
    private UserStatus status;
}
