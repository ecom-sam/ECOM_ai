package com.ecom.ai.ecomassistant.auth.permission;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum DatasetPermission implements Permission {

    DATASET_VIEW("dataset:view", "dataset", "查詢知識庫"),
    DATASET_UPLOAD("dataset:upload", "dataset", "上傳資料集"),
    DATASET_DELETE("dataset:delete", "dataset", "刪除資料集"),
    DATASET_MANAGE("dataset:manage", "dataset", "管理資料集設定");

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