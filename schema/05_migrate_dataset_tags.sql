-- 為現有的 Dataset 資料初始化空的 tags 陣列
UPDATE `ECOM`.`AI`.`dataset` 
SET tags = [] 
WHERE _class = "com.ecom.ai.ecomassistant.db.model.Dataset" 
AND tags IS MISSING;

-- 根據名稱自動設置 tags (可選)
UPDATE `ECOM`.`AI`.`dataset` 
SET tags = ["技術文檔", "API"] 
WHERE _class = "com.ecom.ai.ecomassistant.db.model.Dataset" 
AND (LOWER(name) LIKE "%api%" OR LOWER(name) LIKE "%技術%");

UPDATE `ECOM`.`AI`.`dataset` 
SET tags = ["用戶指南", "手冊"] 
WHERE _class = "com.ecom.ai.ecomassistant.db.model.Dataset" 
AND (LOWER(name) LIKE "%指南%" OR LOWER(name) LIKE "%手冊%");

UPDATE `ECOM`.`AI`.`dataset` 
SET tags = ["FAQ", "問答"] 
WHERE _class = "com.ecom.ai.ecomassistant.db.model.Dataset" 
AND (LOWER(name) LIKE "%faq%" OR LOWER(name) LIKE "%問答%");