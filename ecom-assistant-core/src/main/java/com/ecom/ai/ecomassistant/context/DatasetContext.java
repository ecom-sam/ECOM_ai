package com.ecom.ai.ecomassistant.context;


public class DatasetContext {

    private static final ThreadLocal<DatasetContextData> context = new ThreadLocal<>();

    public static void setDatasetContextData(String datasetId, String documentId) {
        context.set(new DatasetContextData(datasetId, documentId));
    }

    public static DatasetContextData getDatasetContextData() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public record DatasetContextData(
            String datasetId,
            String documentId
    ) {}
}
