CREATE DATABASE  IF NOT EXISTS `greengrocerdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_turkish_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `greengrocerdb`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: greengrocerdb
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `orderinfo`
--

DROP TABLE IF EXISTS `orderinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ordertime` datetime DEFAULT NULL,
  `deliverytime` datetime DEFAULT NULL,
  `products` text,
  `user_id` int DEFAULT NULL,
  `carrier_id` int DEFAULT NULL,
  `isdelivered` tinyint(1) DEFAULT '0',
  `totalcost` double DEFAULT NULL,
  `invoice_content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderinfo`
--

LOCK TABLES `orderinfo` WRITE;
/*!40000 ALTER TABLE `orderinfo` DISABLE KEYS */;
INSERT INTO `orderinfo` VALUES (1,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Elma, 1kg Domates',1,2,1,95,'Fatura No: 101'),(2,'2025-12-20 21:00:41','2025-12-21 21:00:41','3kg Patates, 2kg Soğan',1,2,1,105,'Fatura No: 102'),(3,'2025-12-20 21:00:41','2025-12-22 21:00:41','1kg Muz, 0.5kg Çilek',1,2,0,110,'Fatura No: 103'),(4,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Havuç, 1kg Ispanak',1,2,1,48,'Fatura No: 104'),(5,'2025-12-20 21:00:41','2025-12-21 21:00:41','4kg Portakal',1,2,1,100,'Fatura No: 105'),(6,'2025-12-20 21:00:41','2025-12-22 21:00:41','1kg Sarımsak, 2kg Salatalık',1,2,0,170,'Fatura No: 106'),(7,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Patlıcan, 1kg Biber',1,2,1,125,'Fatura No: 107'),(8,'2025-12-20 21:00:41','2025-12-21 21:00:41','3kg Elma, 2kg Mandalina',1,2,1,130,'Fatura No: 108'),(9,'2025-12-20 21:00:41','2025-12-22 21:00:41','1kg Kivi, 1kg Ananas',1,2,0,185,'Fatura No: 109'),(10,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Armut, 3kg Patates',1,2,1,130,'Fatura No: 110'),(11,'2025-12-20 21:00:41','2025-12-21 21:00:41','1kg Brokoli, 1kg Karnabahar',1,2,1,88,'Fatura No: 111'),(12,'2025-12-20 21:00:41','2025-12-22 21:00:41','5kg Domates',1,2,0,175,'Fatura No: 112'),(13,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Üzüm, 1kg Erik',1,2,1,195,'Fatura No: 113'),(14,'2025-12-20 21:00:41','2025-12-21 21:00:41','1kg Nar, 2kg Ayva',1,2,1,125,'Fatura No: 114'),(15,'2025-12-20 21:00:41','2025-12-22 21:00:41','3kg Havuç, 2kg Soğan',1,2,0,84,'Fatura No: 115'),(16,'2025-12-20 21:00:41','2025-12-21 21:00:41','1kg Çilek, 1kg Muz',1,2,1,155,'Fatura No: 116'),(17,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Pırasa, 1kg Lahana',1,2,1,78,'Fatura No: 117'),(18,'2025-12-20 21:00:41','2025-12-22 21:00:41','4kg Elma',1,2,0,120,'Fatura No: 118'),(19,'2025-12-20 21:00:41','2025-12-21 21:00:41','1kg Kivi, 2kg Portakal',1,2,1,125,'Fatura No: 119'),(20,'2025-12-20 21:00:41','2025-12-21 21:00:41','3kg Salatalık',1,2,1,75,'Fatura No: 120'),(21,'2025-12-20 21:00:41','2025-12-22 21:00:41','2kg Biber, 1kg Patlıcan',1,2,0,130,'Fatura No: 121'),(22,'2025-12-20 21:00:41','2025-12-21 21:00:41','1kg Ananas, 0.5kg Çilek',1,2,1,155,'Fatura No: 122'),(23,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Mandalina, 2kg Elma',1,2,1,100,'Fatura No: 123'),(24,'2025-12-20 21:00:41','2025-12-22 21:00:41','1kg Sarımsak',1,2,0,120,'Fatura No: 124'),(25,'2025-12-20 21:00:41','2025-12-21 21:00:41','2kg Domates, 1kg Salatalık',1,2,1,95,'Fatura No: 125');
/*!40000 ALTER TABLE `orderinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productinfo`
--

DROP TABLE IF EXISTS `productinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` enum('fruit','vegetable') NOT NULL,
  `price` double NOT NULL,
  `stock` double NOT NULL,
  `image` longblob,
  `threshold` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productinfo`
--

LOCK TABLES `productinfo` WRITE;
/*!40000 ALTER TABLE `productinfo` DISABLE KEYS */;
INSERT INTO `productinfo` VALUES (1,'Domates','vegetable',35,100,NULL,5),(2,'Salatalık','vegetable',25,80,NULL,4),(3,'Biber','vegetable',45,60,NULL,3),(4,'Patlıcan','vegetable',40,50,NULL,5),(5,'Patates','vegetable',20,200,NULL,10),(6,'Soğan','vegetable',15,150,NULL,8),(7,'Havuç','vegetable',18,70,NULL,4),(8,'Ispanak','vegetable',30,40,NULL,2),(9,'Pırasa','vegetable',28,45,NULL,3),(10,'Lahana','vegetable',22,55,NULL,5),(11,'Karnabahar','vegetable',38,30,NULL,2),(12,'Brokoli','vegetable',50,25,NULL,2),(13,'Sarımsak','vegetable',120,15,NULL,1),(14,'Elma','fruit',30,120,NULL,6),(15,'Armut','fruit',35,90,NULL,5),(16,'Muz','fruit',65,110,NULL,7),(17,'Portakal','fruit',25,150,NULL,10),(18,'Mandalina','fruit',20,140,NULL,8),(19,'Çilek','fruit',90,40,NULL,3),(20,'Üzüm','fruit',55,60,NULL,4),(21,'Kivi','fruit',75,35,NULL,2),(22,'Ananas','fruit',110,20,NULL,2),(23,'Nar','fruit',45,70,NULL,4),(24,'Ayva','fruit',40,50,NULL,5),(25,'Erik','fruit',85,30,NULL,3);
/*!40000 ALTER TABLE `productinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userinfo`
--

DROP TABLE IF EXISTS `userinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `userinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `role` enum('customer','carrier','owner') NOT NULL,
  `address` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userinfo`
--

LOCK TABLES `userinfo` WRITE;
/*!40000 ALTER TABLE `userinfo` DISABLE KEYS */;
INSERT INTO `userinfo` VALUES (1,'cust','cust','customer','Kadir Has Cad. No:1, Istanbul'),(2,'carr','carr','carrier','Besiktas, Istanbul'),(3,'own','own','owner','Fatih, Istanbul');
/*!40000 ALTER TABLE `userinfo` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-30 19:11:56
