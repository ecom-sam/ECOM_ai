package com.ecom.ai.ecomassistant.auth.permission;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum DatasetPermission implements Permission {

    DATASET_VIEW("dataset:view", "dataset", "查詢知識庫"),
    DATASET_DELETE("dataset:delete", "dataset", "刪除知識庫"),
    DATASET_MANAGE("dataset:manage", "dataset", "知識庫基本訊息管理"),
    DATASET_VISIBILITY_MANAGE("dataset:visibility:manage", "dataset", "知識庫開放設定"),

    DATASET_FILE_UPLOAD("dataset:file:upload", "dataset", "上傳檔案"),
    DATASET_FILE_DELETE("dataset:file:delete", "dataset", "刪除其他人檔案"),
    DATASET_FILE_APPROVE("dataset:file:approve", "dataset", "放行檔案");

    private final String code;
    private final String group;
    private final String label;

    public static Optional<DatasetPermission> fromCode(String code) {
        return Permission.fromCode(DatasetPermission.class, code);
    }

    public static List<DatasetPermission> fromGroup(String group) {
        return Permission.fromGroup(DatasetPermission.class, group);
    }

    public static Optional<DatasetPermission> fromName(String name) {
        return Permission.fromName(DatasetPermission.class, name);
    }
}