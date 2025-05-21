package com.ecom.ai.ecomassistant.db.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private String userInput;
    private boolean resetHistory;
}
