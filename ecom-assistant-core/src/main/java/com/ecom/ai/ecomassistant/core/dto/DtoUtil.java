package com.ecom.ai.ecomassistant.core.dto;

import com.ecom.ai.ecomassistant.db.model.auth.User;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DtoUtil {

    /**
     * 從 userMap 中根據 ID 設定名稱欄位，如果 ID 為 null 或找不到則給空字串。
     *
     * @param userMap       userId -> userName
     * @param idGetter      e.g. dto::getOwnerId
     * @param nameSetter    e.g. dto::setOwnerName
     */
    public static void setUserName(Map<String, User> userMap,
                                   Supplier<String> idGetter,
                                   Consumer<String> nameSetter) {
        String id = idGetter.get();
        String name = Optional.ofNullable(userMap.get(id)).map(User::getName).orElse("");
        nameSetter.accept(name);
    }
}
