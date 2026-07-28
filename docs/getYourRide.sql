CREATE DATABASE  IF NOT EXISTS `shuttle_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `shuttle_db`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: shuttle_db
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `boarding_log`
--

DROP TABLE IF EXISTS `boarding_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `boarding_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `boarded_at` datetime DEFAULT NULL,
  `dropped_off_at` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `fk_log_booking` (`booking_id`),
  CONSTRAINT `fk_log_booking` FOREIGN KEY (`booking_id`) REFERENCES `trip_booking` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `boarding_log`
--

LOCK TABLES `boarding_log` WRITE;
/*!40000 ALTER TABLE `boarding_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `boarding_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driver`
--

DROP TABLE IF EXISTS `driver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver` (
  `driver_id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `join_date` date DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `total_trips` int NOT NULL,
  PRIMARY KEY (`driver_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driver`
--

LOCK TABLES `driver` WRITE;
/*!40000 ALTER TABLE `driver` DISABLE KEYS */;
INSERT INTO `driver` VALUES (1,'Thabo','Nkosi','thabo.nkosi@shuttle.nmu.ac.za','0821234501','SHUTTLE_DRIVER',_binary '','2024-02-01','password123',142),(2,'Nomvula','Dube','nomvula.dube@shuttle.nmu.ac.za','0821234502','SHUTTLE_DRIVER',_binary '','2024-03-15','password123',98),(3,'Sipho','Mabaso','s223456789@mandela.ac.za','0731234503','STUDENT_DRIVER',_binary '','2025-01-20','password123',23),(4,'Aisha','Petersen','s223456790@mandela.ac.za','0731234504','STUDENT_DRIVER',_binary '','2025-02-10','password123',11),(5,'Luyanda','Zulu','s223456791@mandela.ac.za','0731234505','STUDENT_DRIVER',_binary '\0','2026-05-01','password123',0),(6,'Chloe','van der Merwe','s223456792@mandela.ac.za','0731234506','STUDENT_DRIVER',_binary '','2025-08-14','password123',7),(7,'Sam','Driver','sam.driver@example.com','0839876543','STUDENT_DRIVER',_binary '\0','2026-07-01','driverpass',0),(8,'John','Doe','john.doe@shuttle.com','0771122334','SHUTTLE_DRIVER',_binary '','2026-07-06','driver_pwd_1',50),(9,'Sarah','Wilson','sarah.w@shuttle.com','0775566778','STUDENT_DRIVER',_binary '','2026-07-06','driver_pwd_2',12);
/*!40000 ALTER TABLE `driver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driverapplications`
--

DROP TABLE IF EXISTS `driverapplications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driverapplications` (
  `ApplicationID` bigint NOT NULL AUTO_INCREMENT,
  `UserID` bigint NOT NULL,
  `ContactNumber` varchar(20) NOT NULL,
  `VehicleMakeModel` varchar(100) NOT NULL,
  `RegistrationNumber` varchar(30) NOT NULL,
  `SeatingCapacity` int NOT NULL,
  `VehicleColor` varchar(30) NOT NULL,
  `LicenseImagePath` varchar(255) NOT NULL,
  `RegistrationFilePath` varchar(255) NOT NULL,
  `ApplicationStatus` varchar(20) DEFAULT 'Pending Review',
  PRIMARY KEY (`ApplicationID`),
  KEY `fk_application_user` (`UserID`),
  CONSTRAINT `fk_application_user` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driverapplications`
--

LOCK TABLES `driverapplications` WRITE;
/*!40000 ALTER TABLE `driverapplications` DISABLE KEYS */;
/*!40000 ALTER TABLE `driverapplications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shuttle_stop`
--

DROP TABLE IF EXISTS `shuttle_stop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shuttle_stop` (
  `stop_id` bigint NOT NULL AUTO_INCREMENT,
  `stop_name` varchar(255) NOT NULL,
  `area` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`stop_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shuttle_stop`
--

LOCK TABLES `shuttle_stop` WRITE;
/*!40000 ALTER TABLE `shuttle_stop` DISABLE KEYS */;
INSERT INTO `shuttle_stop` VALUES (1,'Korsten','Korsten','PSA, 163 Durban Rd',NULL,NULL),(2,'Sydenham','Korsten','Klesal / PSA, 10 on Smart',NULL,NULL),(3,'Varsity Park','Central','Law Court (Smada)',NULL,NULL),(4,'Richmond Hill','Central','Kalinga House',NULL,NULL),(5,'Russell Road','Central','Home Choice',NULL,NULL),(6,'Feather Market Hall','Central','Govan Mbeki Ave',NULL,NULL),(7,'Rink Street','Central','The Suites',NULL,NULL),(8,'Central','Central','Belmont Terrace',NULL,NULL),(9,'Walmer','Walmer','PSA',NULL,NULL),(10,'Walmer Blvd','Walmer','Shell Garage',NULL,NULL),(11,'Humewood','Humewood','Kings Beach',NULL,NULL),(12,'Pier 14','Pier 14','Pier 14',NULL,NULL),(13,'Forest Hill','Forest Hill','Garage, Morestond Flats and Stadium',NULL,NULL),(14,'Summerstrand','Summerstrand','Summerbreeze Spar',NULL,NULL),(15,'SSSV Residences','Summerstrand','Summerstrand',NULL,NULL),(16,'South Campus','Summerstrand','Summerstrand',NULL,NULL),(17,'North Campus','Summerstrand','Summerstrand',NULL,NULL),(18,'Summerstrand Campus','Summerstrand','Campus Pick n Pay',NULL,NULL),(19,'Gomery Shuttle Stop','Summerstrand','Gomery Place / Omega / Dunes',NULL,NULL);
/*!40000 ALTER TABLE `shuttle_stop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shuttle_time_slot`
--

DROP TABLE IF EXISTS `shuttle_time_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shuttle_time_slot` (
  `slot_id` bigint NOT NULL AUTO_INCREMENT,
  `period` enum('Morning','Afternoon') NOT NULL,
  `departs` time NOT NULL,
  `arrives` time NOT NULL,
  PRIMARY KEY (`slot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shuttle_time_slot`
--

LOCK TABLES `shuttle_time_slot` WRITE;
/*!40000 ALTER TABLE `shuttle_time_slot` DISABLE KEYS */;
INSERT INTO `shuttle_time_slot` VALUES (1,'Morning','06:45:00','07:30:00'),(2,'Morning','07:45:00','08:30:00'),(3,'Morning','08:45:00','09:30:00'),(4,'Morning','09:45:00','10:30:00'),(5,'Afternoon','12:30:00','13:15:00'),(6,'Afternoon','14:30:00','15:15:00'),(7,'Afternoon','16:00:00','16:45:00'),(8,'Afternoon','17:30:00','18:15:00');
/*!40000 ALTER TABLE `shuttle_time_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `student_id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `student_number` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_funded` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `student_number` (`student_number`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'Kelvin','Mudzingwa','test@mandela.ac.za','0821234567','S12345678','2026-06-30 17:11:28.998438',_binary '\0','test123'),(2,'ghon','ktwl','testinge@mandela.ac.za','','2250418','2026-07-01 13:13:10.021605',_binary '\0','rxbjytv'),(3,'Alice','Johnson','alice.j@example.edu','0123456789','20210001','2026-07-06 16:33:37.000000',_binary '','hashed_pwd_1'),(4,'Bob','Smith','bob.s@example.edu','0987654321','20210002','2026-07-06 16:33:37.000000',_binary '\0','hashed_pwd_2'),(5,'Charlie','Davis','charlie.d@example.edu','0112233445','20210003','2026-07-06 16:33:37.000000',_binary '','hashed_pwd_3'),(6,'student','m','nsfas@mandela.ac.za','','22501962','2026-07-16 18:34:22.509446',_binary '\0','testing123'),(7,'test','test','nsfas1@mandela.ac.za','','1284069','2026-07-18 22:49:26.838489',_binary '\0','1234557'),(8,'test','2','testionhg@mandela.ac.za','','49046','2026-07-18 22:58:03.311934',_binary '\0','tbsjbs'),(9,'the','test','hel@mandela.ac.za','','28013','2026-07-18 23:02:19.625642',_binary '\0','qtgkr'),(10,'youn','lee','younglee@mandela.ac.za','','28053','2026-07-18 23:10:02.284370',_binary '','test123');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trip`
--

DROP TABLE IF EXISTS `trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip` (
  `trip_id` bigint NOT NULL AUTO_INCREMENT,
  `driver_id` bigint NOT NULL,
  `registration_number` varchar(20) NOT NULL,
  `trip_type` varchar(255) NOT NULL,
  `slot_id` bigint DEFAULT NULL,
  `departure_stop` varchar(255) NOT NULL,
  `destination_stop` varchar(255) NOT NULL,
  `departure_time` datetime DEFAULT NULL,
  `arrival_time` datetime DEFAULT NULL,
  `available_seats` int DEFAULT NULL,
  `price` decimal(8,2) DEFAULT '0.00',
  `status` varchar(255) NOT NULL,
  `departure_lat` double DEFAULT NULL,
  `departure_lng` double DEFAULT NULL,
  `destination_lat` double DEFAULT NULL,
  `destination_lng` double DEFAULT NULL,
  PRIMARY KEY (`trip_id`),
  KEY `fk_trip_driver` (`driver_id`),
  KEY `fk_trip_vehicle` (`registration_number`),
  KEY `fk_trip_slot` (`slot_id`),
  CONSTRAINT `fk_trip_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`driver_id`),
  CONSTRAINT `fk_trip_slot` FOREIGN KEY (`slot_id`) REFERENCES `shuttle_time_slot` (`slot_id`),
  CONSTRAINT `fk_trip_vehicle` FOREIGN KEY (`registration_number`) REFERENCES `vehicle` (`registration_number`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip`
--

LOCK TABLES `trip` WRITE;
/*!40000 ALTER TABLE `trip` DISABLE KEYS */;
INSERT INTO `trip` VALUES (1,3,'CA123456','Carpool',NULL,'Kwazakhele, Ngxabane Street','South Campus','2026-07-01 07:15:00','2026-07-06 16:38:57',3,25.00,'COMPLETED',NULL,NULL,NULL,NULL),(2,3,'CA123456','Carpool',NULL,'South Campus','Kwazakhele, Ngxabane Street','2026-07-01 17:00:00',NULL,3,25.00,'CANCELLED',NULL,NULL,NULL,NULL),(3,4,'CA654321','Carpool',NULL,'Newton Park, Cape Road Spar','2nd Avenue Campus','2026-07-01 08:00:00',NULL,0,30.00,'SCHEDULED',NULL,NULL,NULL,NULL),(4,4,'CA654321','Carpool',NULL,'Newton Park, Cape Road Spar','North Campus','2026-06-29 08:00:00','2026-06-29 08:25:00',2,30.00,'Completed',NULL,NULL,NULL,NULL),(5,6,'CA999888','Carpool',NULL,'Summerstrand, Marine Drive','South Campus','2026-06-30 07:30:00',NULL,3,20.00,'Cancelled',NULL,NULL,NULL,NULL),(6,6,'CA999888','Carpool',NULL,'Walmer, 6th Avenue','South Campus','2026-07-02 07:00:00',NULL,1,20.00,'SCHEDULED',NULL,NULL,NULL,NULL),(7,7,'CB123456','STUDENT_DRIVER',NULL,'Bird Street, Gqeberha','Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',NULL,NULL,NULL,NULL),(13,7,'CB123456','STUDENT_DRIVER',NULL,'Bird Street, Gqeberha','Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',NULL,NULL,NULL,NULL),(14,7,'CB123456','STUDENT_DRIVER',NULL,'Bird Street, Gqeberha','Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',NULL,NULL,NULL,NULL),(20,7,'CB123456','STUDENT_DRIVER',NULL,'Bird Street, Gqeberha','Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',NULL,NULL,NULL,NULL),(21,7,'CB123456','STUDENT_DRIVER',NULL,'Bird Street, Gqeberha','Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',-33.9581,25.6011,-33.9997,25.6698),(22,7,'CB123456','Carpool',NULL,'4GR72J96+C4 Walmer, Gqeberha','4GR72M29+CX Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',-33.9814,25.6103,-33.9989,25.6699),(23,7,'CB123456','Carpool',NULL,'4GR72J96+C4 Walmer, Gqeberha','4GR72M29+CX Summerstrand, Gqeberha','2026-07-01 14:00:00',NULL,2,30.00,'CONFIRMED',-33.9814,25.6103,-33.9989,25.6699),(24,1,'ABC 123 EC','SHUTTLE',NULL,'North Campus','South Campus','2026-07-06 17:33:37',NULL,1,0.00,'SCHEDULED',-33.9912,25.6698,-33.9984,25.675),(25,2,'XYZ 789 EC','PRIVATE',NULL,'Summerstrand','Missionvale','2026-07-06 18:33:37',NULL,2,15.50,'SCHEDULED',-34.0021,25.6601,-33.91,25.55);
/*!40000 ALTER TABLE `trip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trip_booking`
--

DROP TABLE IF EXISTS `trip_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip_booking` (
  `booking_id` bigint NOT NULL AUTO_INCREMENT,
  `trip_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `booking_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `booking_status` enum('Pending','Confirmed','Cancelled') DEFAULT 'Pending',
  PRIMARY KEY (`booking_id`),
  KEY `fk_booking_trip` (`trip_id`),
  KEY `fk_booking_student` (`student_id`),
  CONSTRAINT `fk_booking_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_trip` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip_booking`
--

LOCK TABLES `trip_booking` WRITE;
/*!40000 ALTER TABLE `trip_booking` DISABLE KEYS */;
/*!40000 ALTER TABLE `trip_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trip_review`
--

DROP TABLE IF EXISTS `trip_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip_review` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `rating` int DEFAULT NULL,
  `review` text,
  `review_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `booking_id` (`booking_id`),
  CONSTRAINT `fk_review_booking` FOREIGN KEY (`booking_id`) REFERENCES `trip_booking` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `trip_review_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip_review`
--

LOCK TABLES `trip_review` WRITE;
/*!40000 ALTER TABLE `trip_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `trip_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trip_stop`
--

DROP TABLE IF EXISTS `trip_stop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip_stop` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trip_id` bigint NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `stop_name` varchar(255) DEFAULT NULL,
  `stop_order` int NOT NULL,
  `student_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trip_stop_trip_id` (`trip_id`),
  KEY `FKlx3nwtyilnv0m6newdeqfa76u` (`student_id`),
  CONSTRAINT `fk_trip_stop_trip` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`) ON DELETE CASCADE,
  CONSTRAINT `FKlx3nwtyilnv0m6newdeqfa76u` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip_stop`
--

LOCK TABLES `trip_stop` WRITE;
/*!40000 ALTER TABLE `trip_stop` DISABLE KEYS */;
INSERT INTO `trip_stop` VALUES (1,1,-33.9912,25.6698,'North Campus Gate 1',1,NULL),(2,1,-33.995,25.672,'Library Stop',2,NULL),(3,1,-33.9984,25.675,'South Campus Terminal',3,NULL),(4,2,-34.0021,25.6601,'Student House A',1,1),(5,2,-33.91,25.55,'Missionvale Campus',2,NULL),(6,3,0,0,'Killarney Road, South End, Gqeberha, 6013, South Africa',1,1),(7,3,0,0,'Waterfront, Cape Town, WC, South Africa',2,1),(8,6,0,0,'Killarney Road, South End, Gqeberha, 6013, South Africa',1,1),(9,24,0,0,'Summerstrand, Gqeberha, EC, South Africa',1,1),(10,24,-33.9912,25.6698,'North Campus',2,1),(11,24,-33.9912,25.6698,'North Campus',3,1),(12,24,-33.9912,25.6698,'North Campus',4,1),(13,24,-33.9912,25.6698,'North Campus',5,1),(14,24,-33.9912,25.6698,'North Campus',6,1),(15,24,-33.9912,25.6698,'North Campus',7,1),(16,24,-33.9912,25.6698,'North Campus',8,1),(17,24,-33.9912,25.6698,'North Campus',9,1),(18,6,0,0,'Killarney, Rosebank, Johannesburg, GT, South Africa',2,1),(19,25,-34.0021,25.6601,'Summerstrand',1,1);
/*!40000 ALTER TABLE `trip_stop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserID` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT,
  `driver_id` bigint NOT NULL,
  `registration_number` varchar(20) NOT NULL,
  `model` varchar(255) DEFAULT NULL,
  `vehicle_year` int DEFAULT NULL,
  `colour` varchar(255) DEFAULT NULL,
  `capacity` int NOT NULL,
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `registration_number` (`registration_number`),
  KEY `fk_vehicle_driver` (`driver_id`),
  CONSTRAINT `fk_vehicle_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`driver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle`
--

LOCK TABLES `vehicle` WRITE;
/*!40000 ALTER TABLE `vehicle` DISABLE KEYS */;
INSERT INTO `vehicle` VALUES (1,1,'NMU001EC','Toyota Quantum',2021,'White',15),(2,2,'NMU002EC','Toyota Quantum',2022,'White',15),(3,3,'CA123456','VW Polo Vivo',2019,'Silver',4),(4,4,'CA654321','Toyota Corolla',2020,'Blue',4),(5,5,'CA111222','Hyundai i20',2018,'Red',4),(6,6,'CA999888','Ford Fiesta',2017,'Black',4),(7,7,'CB123456','Toyota Corolla',2022,'White',4),(8,1,'ABC 123 EC','Toyota Quantum',2020,'White',15),(9,2,'XYZ 789 EC','VW Polo',2018,'Silver',4);
/*!40000 ALTER TABLE `vehicle` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-27  9:20:41
