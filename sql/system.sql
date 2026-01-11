-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bill_manager
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `budget`
--

DROP TABLE IF EXISTS `budget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `budget` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预算ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `amount` decimal(10,2) NOT NULL COMMENT '预算金额',
  `budget_month` varchar(7) NOT NULL COMMENT '预算年月（格式：YYYY-MM）',
  `spent` decimal(10,2) DEFAULT '0.00' COMMENT '已支出金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category_month` (`user_id`,`category_id`,`budget_month`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_budget_month` (`budget_month`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预算表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `budget`
--

LOCK TABLES `budget` WRITE;
/*!40000 ALTER TABLE `budget` DISABLE KEYS */;
INSERT INTO `budget` VALUES (1,1,1,2000.00,'2026-01',0.00,NULL,'2026-01-08 17:38:06','2026-01-10 18:56:12'),(2,1,2,60.00,'2026-01',0.00,NULL,'2026-01-08 19:00:54','2026-01-08 19:00:54'),(3,1,5,1000.00,'2026-01',0.00,NULL,'2026-01-10 09:11:37','2026-01-10 14:55:47'),(5,1,5,1000.00,'2026-02',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(6,1,5,1000.00,'2026-03',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(7,1,5,1000.00,'2026-04',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(8,1,5,1000.00,'2026-05',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(9,1,5,1000.00,'2026-06',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(10,1,5,1000.00,'2026-07',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(11,1,5,1000.00,'2026-08',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(12,1,5,1000.00,'2026-09',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(13,1,5,1000.00,'2026-10',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(14,1,5,1000.00,'2026-11',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(15,1,5,1000.00,'2026-12',0.00,NULL,'2026-01-10 14:36:34','2026-01-10 14:55:47'),(84,1,1,2000.00,'2026-02',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(85,1,1,2000.00,'2026-03',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(86,1,1,2000.00,'2026-04',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(87,1,1,2000.00,'2026-05',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(88,1,1,2000.00,'2026-06',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(89,1,1,2000.00,'2026-07',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(90,1,1,2000.00,'2026-08',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(91,1,1,2000.00,'2026-09',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(92,1,1,2000.00,'2026-10',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(93,1,1,2000.00,'2026-11',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12'),(94,1,1,2000.00,'2026-12',0.00,NULL,'2026-01-10 18:56:12','2026-01-10 18:56:12');
/*!40000 ALTER TABLE `budget` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expense`
--

DROP TABLE IF EXISTS `expense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支出ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支出金额',
  `transaction_date` date NOT NULL COMMENT '交易日期',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expense`
--

LOCK TABLES `expense` WRITE;
/*!40000 ALTER TABLE `expense` DISABLE KEYS */;
INSERT INTO `expense` VALUES (1,1,1,20.00,'2026-01-08','','2026-01-08 17:36:36','2026-01-08 17:36:36'),(2,1,2,9.00,'2026-01-08','','2026-01-08 17:37:24','2026-01-08 17:37:24'),(3,1,5,1000.00,'2026-01-10','','2026-01-10 09:09:56','2026-01-10 09:09:56'),(4,1,1,5.00,'2026-01-08','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(5,1,1,15.00,'2026-01-08','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(6,1,1,5.00,'2026-01-08','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(7,1,1,15.00,'2026-01-08','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(8,1,1,15.00,'2026-01-08','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(9,1,1,0.10,'2025-12-05','美团','2026-01-10 11:49:37','2026-01-10 11:49:37'),(10,1,7,222.00,'2026-02-11','','2026-01-10 14:05:07','2026-01-10 14:05:07');
/*!40000 ALTER TABLE `expense` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expense_category`
--

DROP TABLE IF EXISTS `expense_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expense_category`
--

LOCK TABLES `expense_category` WRITE;
/*!40000 ALTER TABLE `expense_category` DISABLE KEYS */;
INSERT INTO `expense_category` VALUES (1,1,'餐饮','餐饮消费','2026-01-08 17:32:36','2026-01-08 17:32:36'),(2,1,'交通','交通出行','2026-01-08 17:32:36','2026-01-08 17:32:36'),(3,1,'购物','购物消费','2026-01-08 17:32:36','2026-01-08 17:32:36'),(4,1,'娱乐','娱乐消费','2026-01-08 17:32:36','2026-01-08 17:32:36'),(5,1,'住房','住房相关支出','2026-01-08 17:32:36','2026-01-08 17:32:36'),(6,1,'医疗','医疗健康','2026-01-08 17:32:36','2026-01-08 17:32:36'),(7,1,'教育','教育培训','2026-01-08 17:32:36','2026-01-08 17:32:36'),(8,1,'其他','其他支出','2026-01-08 17:32:36','2026-01-08 17:32:36');
/*!40000 ALTER TABLE `expense_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `income`
--

DROP TABLE IF EXISTS `income`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `income` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收入ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `amount` decimal(10,2) NOT NULL COMMENT '收入金额',
  `transaction_date` date NOT NULL COMMENT '交易日期',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收入记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `income`
--

LOCK TABLES `income` WRITE;
/*!40000 ALTER TABLE `income` DISABLE KEYS */;
INSERT INTO `income` VALUES (1,1,1,6000.00,'2026-01-08','','2026-01-08 17:36:06','2026-01-08 17:36:58'),(2,1,5,5.00,'2026-01-30','美团','2026-01-10 11:49:37','2026-01-10 14:07:44'),(3,1,1,0.62,'2026-01-06','商家转账-来自拼多多','2026-01-10 11:49:37','2026-01-10 11:49:37');
/*!40000 ALTER TABLE `income` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `income_category`
--

DROP TABLE IF EXISTS `income_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `income_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收入分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `income_category`
--

LOCK TABLES `income_category` WRITE;
/*!40000 ALTER TABLE `income_category` DISABLE KEYS */;
INSERT INTO `income_category` VALUES (1,1,'工资','工资收入','2026-01-08 17:32:36','2026-01-08 17:32:36'),(2,1,'奖金','奖金收入','2026-01-08 17:32:36','2026-01-08 17:32:36'),(3,1,'投资收益','投资理财收益','2026-01-08 17:32:36','2026-01-08 17:32:36'),(4,1,'兼职收入','兼职工作收入','2026-01-08 17:32:36','2026-01-08 17:32:36'),(5,1,'其他','其他收入','2026-01-08 17:32:36','2026-01-08 17:32:36');
/*!40000 ALTER TABLE `income_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax_record`
--

DROP TABLE IF EXISTS `tax_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '税务记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tax_type` varchar(50) NOT NULL COMMENT '税种',
  `taxable_amount` decimal(10,2) NOT NULL COMMENT '应税金额',
  `tax_rate` decimal(5,4) NOT NULL COMMENT '税率',
  `tax_amount` decimal(10,2) NOT NULL COMMENT '税额',
  `tax_date` date NOT NULL COMMENT '税务日期',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tax_date` (`tax_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='税务记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax_record`
--

LOCK TABLES `tax_record` WRITE;
/*!40000 ALTER TABLE `tax_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `tax_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax_rule`
--

DROP TABLE IF EXISTS `tax_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tax_type` varchar(50) NOT NULL COMMENT '税种',
  `tax_rate` decimal(5,4) NOT NULL COMMENT '税率',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最小金额',
  `max_amount` decimal(10,2) DEFAULT NULL COMMENT '最大金额',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tax_type` (`tax_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='税率规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax_rule`
--

LOCK TABLES `tax_rule` WRITE;
/*!40000 ALTER TABLE `tax_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `tax_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13800138000','admin@example.com',1,'2026-01-08 17:32:36','2026-01-08 17:32:36');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_target`
--

DROP TABLE IF EXISTS `user_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_target` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_month` varchar(7) NOT NULL COMMENT '目标年月（格式：YYYY-MM）',
  `income_target` decimal(10,2) NOT NULL COMMENT '收入目标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_month` (`user_id`,`target_month`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_month` (`target_month`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收入目标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_target`
--

LOCK TABLES `user_target` WRITE;
/*!40000 ALTER TABLE `user_target` DISABLE KEYS */;
INSERT INTO `user_target` VALUES (1,1,'2026-01',6000.00,'2026-01-08 17:32:36','2026-01-10 14:16:57');
/*!40000 ALTER TABLE `user_target` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-10 21:13:13
