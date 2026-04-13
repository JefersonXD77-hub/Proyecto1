-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: Horizontes_sin_limites
-- ------------------------------------------------------
-- Server version	8.0.45

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
-- Table structure for table `cancelacion`
--

DROP TABLE IF EXISTS `cancelacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cancelacion` (
  `id_cancelacion` int NOT NULL AUTO_INCREMENT,
  `id_reservacion` int NOT NULL,
  `fecha_cancelacion` date NOT NULL,
  `dias_anticipacion` int NOT NULL,
  `porcentaje_reembolso` decimal(5,2) NOT NULL,
  `monto_pagado` decimal(10,2) NOT NULL,
  `monto_reembolsado` decimal(10,2) NOT NULL,
  `perdida_agencia` decimal(10,2) NOT NULL,
  `id_usuario_proceso` int NOT NULL,
  PRIMARY KEY (`id_cancelacion`),
  UNIQUE KEY `uq_cancelacion_reservacion` (`id_reservacion`),
  KEY `fk_cancelacion_usuario` (`id_usuario_proceso`),
  KEY `idx_cancelacion_fecha` (`fecha_cancelacion`),
  CONSTRAINT `fk_cancelacion_reservacion` FOREIGN KEY (`id_reservacion`) REFERENCES `reservacion` (`id_reservacion`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_cancelacion_usuario` FOREIGN KEY (`id_usuario_proceso`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_cancelacion_dias` CHECK ((`dias_anticipacion` >= 0)),
  CONSTRAINT `chk_cancelacion_monto_pagado` CHECK ((`monto_pagado` >= 0)),
  CONSTRAINT `chk_cancelacion_monto_reembolsado` CHECK ((`monto_reembolsado` >= 0)),
  CONSTRAINT `chk_cancelacion_perdida` CHECK ((`perdida_agencia` >= 0)),
  CONSTRAINT `chk_cancelacion_porcentaje` CHECK (((`porcentaje_reembolso` >= 0) and (`porcentaje_reembolso` <= 100)))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cancelacion`
--

LOCK TABLES `cancelacion` WRITE;
/*!40000 ALTER TABLE `cancelacion` DISABLE KEYS */;
INSERT INTO `cancelacion` VALUES (1,1,'2026-04-02',13,40.00,4250.00,1700.00,2550.00,1),(2,4,'2026-04-09',357,100.00,0.00,0.00,0.00,1),(3,3,'2026-04-09',11,40.00,0.00,0.00,0.00,1),(4,6,'2026-04-10',155,100.00,0.00,0.00,0.00,4);
/*!40000 ALTER TABLE `cancelacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `dpi_pasaporte` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre_completo` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `telefono` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nacionalidad` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `uq_cliente_dpi_pasaporte` (`dpi_pasaporte`),
  UNIQUE KEY `uq_cliente_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'1234567890101','Juan Carlos Perez Lopez','1998-05-10','5555-9999','juan.perez.lopez@gmail.com','Guatemalteca',0),(3,'1234567890199','Manuel Aguilar Saquic','1988-10-11','5555-1237','manuel@gmail.com','Guatemalteca',1),(5,'89898989','Anita Nueva','2000-01-01','13467912','anita@gmail.com','guatemalteca',1),(6,'1234567894561230','Cosme Fulanito','2003-02-01','01233210','fulanito@gmail.com','guatemalteco',1),(7,'1234567899877','Cesar Milian','1900-01-01','15146332','milian@gmail.com','guatemalteco',1);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destino`
--

DROP TABLE IF EXISTS `destino`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `destino` (
  `id_destino` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pais` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `clima_epoca` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `url_imagen` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_destino`),
  UNIQUE KEY `uq_destino_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destino`
--

LOCK TABLES `destino` WRITE;
/*!40000 ALTER TABLE `destino` DISABLE KEYS */;
INSERT INTO `destino` VALUES (1,'Antigua Guatemala','Guatemala','Ciudad colonial ideal para turismo cultural y gastronómico.','Templado durante la mayor parte del año.','https://ejemplo.com/antigua.jpg',1),(2,'Chichicastenango','Guatemala','Destino colonial.','Templado y agradable, ideal entre noviembre y abril.','https://ejemplo.com/chichi.jpg',0),(3,'Tikal','Guatemala','Un interesante lugar para visitar.','Templado','c:/Users/Jeff/Escritorio/imagenTikal.png',1),(4,'Cancún','México','Playas del Caribe mexicano','',NULL,1),(5,'CUNOC','Guatemala','Un lugar que te atrapa por años','Soleado','cunoc/imagen.png',1),(6,'Cataratas del Niagara Pez','Canadá','Disfruta de una visita a la emblemática catarata.','Clima templado en el mes de Junio','pez/imagen.jpg',1);
/*!40000 ALTER TABLE `destino` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_reservacion`
--

DROP TABLE IF EXISTS `estado_reservacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_reservacion` (
  `id_estado_reservacion` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_estado_reservacion`),
  UNIQUE KEY `uq_estado_reservacion_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_reservacion`
--

LOCK TABLES `estado_reservacion` WRITE;
/*!40000 ALTER TABLE `estado_reservacion` DISABLE KEYS */;
INSERT INTO `estado_reservacion` VALUES (3,'CANCELADA'),(4,'COMPLETADA'),(2,'CONFIRMADA'),(1,'PENDIENTE');
/*!40000 ALTER TABLE `estado_reservacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metodo_pago`
--

DROP TABLE IF EXISTS `metodo_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `metodo_pago` (
  `id_metodo_pago` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_metodo_pago`),
  UNIQUE KEY `uq_metodo_pago_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metodo_pago`
--

LOCK TABLES `metodo_pago` WRITE;
/*!40000 ALTER TABLE `metodo_pago` DISABLE KEYS */;
INSERT INTO `metodo_pago` VALUES (1,'EFECTIVO'),(2,'TARJETA'),(3,'TRANSFERENCIA');
/*!40000 ALTER TABLE `metodo_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `id_pago` int NOT NULL AUTO_INCREMENT,
  `id_reservacion` int NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `id_metodo_pago` int NOT NULL,
  `fecha_pago` date NOT NULL,
  PRIMARY KEY (`id_pago`),
  KEY `idx_pago_reservacion` (`id_reservacion`),
  KEY `idx_pago_metodo` (`id_metodo_pago`),
  KEY `idx_pago_fecha_pago` (`fecha_pago`),
  CONSTRAINT `fk_pago_metodo` FOREIGN KEY (`id_metodo_pago`) REFERENCES `metodo_pago` (`id_metodo_pago`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pago_reservacion` FOREIGN KEY (`id_reservacion`) REFERENCES `reservacion` (`id_reservacion`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_pago_monto` CHECK ((`monto` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
INSERT INTO `pago` VALUES (1,1,1250.00,2,'2026-03-20'),(2,1,500.00,1,'2026-04-01'),(3,1,1000.00,2,'2026-04-02'),(4,1,500.00,1,'2026-04-01'),(5,1,1000.00,2,'2026-04-02'),(6,2,1500.00,1,'2026-04-04'),(7,5,55000.00,3,'2026-04-10'),(8,5,500.00,1,'2026-04-10'),(9,7,4.00,2,'2026-04-10');
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paquete_turistico`
--

DROP TABLE IF EXISTS `paquete_turistico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paquete_turistico` (
  `id_paquete` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_destino` int NOT NULL,
  `duracion_dias` int NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `capacidad_maxima` int NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_paquete`),
  UNIQUE KEY `uq_paquete_nombre` (`nombre`),
  KEY `idx_paquete_destino` (`id_destino`),
  CONSTRAINT `fk_paquete_destino` FOREIGN KEY (`id_destino`) REFERENCES `destino` (`id_destino`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_paquete_capacidad` CHECK ((`capacidad_maxima` > 0)),
  CONSTRAINT `chk_paquete_duracion` CHECK ((`duracion_dias` > 0)),
  CONSTRAINT `chk_paquete_precio` CHECK ((`precio_venta` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paquete_turistico`
--

LOCK TABLES `paquete_turistico` WRITE;
/*!40000 ALTER TABLE `paquete_turistico` DISABLE KEYS */;
INSERT INTO `paquete_turistico` VALUES (1,'Escapada Antigua 3D/2N',1,3,'Incluye hospedaje, recorrido cultural y desayuno.',1250.00,20,0),(2,'Aventura en Antigua Premium',1,4,'Paquete turistico premium en Antigua Guatemala con actividades adicionales.',1500.00,18,1),(3,'Aventura Tikal',3,5,'Viaje turístico a Petén por una semana.',10000.00,5,1),(4,'Caribe Mágico 7 noches',4,7,'',18500.00,20,1),(5,'Tour al CUNOC',5,1,'Date una vuelta al CUNOC, solo hay que pagar el pasaje recién subido de los buses.',3.50,1,1),(6,'Viaje Cataratas',6,3,'Viaja hacia la emblemática catarata del Niagara pez.',165.00,3,1);
/*!40000 ALTER TABLE `paquete_turistico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `id_proveedor` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_tipo_proveedor` int NOT NULL,
  `pais_operacion` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contacto` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_proveedor`),
  UNIQUE KEY `uq_proveedor_nombre` (`nombre`),
  KEY `idx_proveedor_tipo` (`id_tipo_proveedor`),
  CONSTRAINT `fk_proveedor_tipo` FOREIGN KEY (`id_tipo_proveedor`) REFERENCES `tipo_proveedor` (`id_tipo_proveedor`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (1,'Avianca Guatemala',1,'Guatemala','corporativo@avianca.com',1),(3,'Avianca',1,'Guatemala','ventas@avianca.com',1),(4,'Delta',1,'USA','delta@gmail.com',1),(5,'TACA Airlines',1,'Guatemala','',1),(6,'Bus Ruta 20',4,'Guatemala','ruta20@gmail.com',1),(7,'Milo Travel\'s',4,'USA','milo@cunoc.edu.gt',1);
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservacion`
--

DROP TABLE IF EXISTS `reservacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservacion` (
  `id_reservacion` int NOT NULL AUTO_INCREMENT,
  `numero_reservacion` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_viaje` date NOT NULL,
  `id_paquete` int NOT NULL,
  `cantidad_pasajeros` int NOT NULL,
  `id_agente_usuario` int NOT NULL,
  `costo_total` decimal(10,2) NOT NULL,
  `id_estado_reservacion` int NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_reservacion`),
  UNIQUE KEY `uq_reservacion_numero` (`numero_reservacion`),
  KEY `idx_reservacion_paquete` (`id_paquete`),
  KEY `idx_reservacion_agente` (`id_agente_usuario`),
  KEY `idx_reservacion_estado` (`id_estado_reservacion`),
  KEY `idx_reservacion_fecha_viaje` (`fecha_viaje`),
  CONSTRAINT `fk_reservacion_agente` FOREIGN KEY (`id_agente_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reservacion_estado` FOREIGN KEY (`id_estado_reservacion`) REFERENCES `estado_reservacion` (`id_estado_reservacion`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reservacion_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquete_turistico` (`id_paquete`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_reservacion_cantidad_pasajeros` CHECK ((`cantidad_pasajeros` > 0)),
  CONSTRAINT `chk_reservacion_costo_total` CHECK ((`costo_total` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservacion`
--

LOCK TABLES `reservacion` WRITE;
/*!40000 ALTER TABLE `reservacion` DISABLE KEYS */;
INSERT INTO `reservacion` VALUES (1,'RES-0001','2026-03-27 01:46:27','2026-04-15',1,1,2,1250.00,3,1),(2,'RES-1775005855088','2026-04-01 01:10:55','2026-04-20',2,1,1,1500.00,2,1),(3,'RES-1775360352305','2026-04-05 03:39:12','2026-04-20',2,1,1,1500.00,3,1),(4,'RES-1775753111345','2026-04-09 16:45:11','2027-04-01',2,1,1,1500.00,3,1),(5,'RES-1775836255284','2026-04-10 15:50:55','2026-04-30',4,3,1,55500.00,2,1),(6,'RES-1775836851395','2026-04-10 16:00:51','2026-09-12',5,1,4,3.50,3,1),(7,'RES-1775836871390','2026-04-10 16:01:11','2026-01-01',5,1,4,3.50,2,1);
/*!40000 ALTER TABLE `reservacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservacion_pasajero`
--

DROP TABLE IF EXISTS `reservacion_pasajero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservacion_pasajero` (
  `id_reservacion` int NOT NULL,
  `id_cliente` int NOT NULL,
  PRIMARY KEY (`id_reservacion`,`id_cliente`),
  KEY `fk_reservacion_pasajero_cliente` (`id_cliente`),
  CONSTRAINT `fk_reservacion_pasajero_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reservacion_pasajero_reservacion` FOREIGN KEY (`id_reservacion`) REFERENCES `reservacion` (`id_reservacion`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservacion_pasajero`
--

LOCK TABLES `reservacion_pasajero` WRITE;
/*!40000 ALTER TABLE `reservacion_pasajero` DISABLE KEYS */;
INSERT INTO `reservacion_pasajero` VALUES (1,1),(2,3),(3,3),(5,3),(4,5),(5,5),(5,6),(7,6),(6,7);
/*!40000 ALTER TABLE `reservacion_pasajero` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_rol` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `uq_rol_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMINISTRADOR'),(2,'ATENCION_CLIENTE'),(3,'OPERACIONES');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicio_paquete`
--

DROP TABLE IF EXISTS `servicio_paquete`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicio_paquete` (
  `id_servicio_paquete` int NOT NULL AUTO_INCREMENT,
  `id_paquete` int NOT NULL,
  `id_proveedor` int NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `costo` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_servicio_paquete`),
  KEY `idx_servicio_paquete_paquete` (`id_paquete`),
  KEY `idx_servicio_paquete_proveedor` (`id_proveedor`),
  CONSTRAINT `fk_servicio_paquete_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquete_turistico` (`id_paquete`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_servicio_paquete_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_servicio_paquete_costo` CHECK ((`costo` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicio_paquete`
--

LOCK TABLES `servicio_paquete` WRITE;
/*!40000 ALTER TABLE `servicio_paquete` DISABLE KEYS */;
INSERT INTO `servicio_paquete` VALUES (2,1,1,'Hospedaje 2 noches',450.00);
/*!40000 ALTER TABLE `servicio_paquete` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_proveedor`
--

DROP TABLE IF EXISTS `tipo_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_proveedor` (
  `id_tipo_proveedor` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_tipo_proveedor`),
  UNIQUE KEY `uq_tipo_proveedor_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_proveedor`
--

LOCK TABLES `tipo_proveedor` WRITE;
/*!40000 ALTER TABLE `tipo_proveedor` DISABLE KEYS */;
INSERT INTO `tipo_proveedor` VALUES (1,'AEROLINEA'),(2,'HOTEL'),(5,'OTRO'),(3,'TOUR'),(4,'TRASLADO');
/*!40000 ALTER TABLE `tipo_proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre_completo` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_rol` int NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuario_username` (`username`),
  UNIQUE KEY `uq_usuario_correo` (`correo`),
  KEY `idx_usuario_rol` (`id_rol`),
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'admin','$2a$10$SspeMZ1MsDKYwlPfefDFYOYAouye4FvqndLTJt073wXBOzn.fs1aK','Administrador General','admin@horizontes.com',1,1,'2026-03-27 01:39:48'),(2,'agente1','$2a$10$ewLJyJZZ5/7BsNE36Ed5VuofK2xvVR22L4YiFLY1P8O3DuykRL6E2','María López','agente1@horizontes.com',2,1,'2026-03-27 01:46:27'),(3,'operador1','$2a$10$u6mmnsZby9hU5k9TVcaza.Epn4rgoNkhxj8utlxtHO2eDMo9Tdvn2','Operador Uno Actualizado','operador1.actualizado@horizontes.com',3,1,'2026-04-06 18:24:12'),(4,'jperez','$2a$10$weeruwMB4yCO4MEyywDrT.Fvc4liDDbABzKmYIu2pyupZtb4pdy7S','jperez',NULL,2,1,'2026-04-10 06:06:45');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vw_paquete_costos`
--

DROP TABLE IF EXISTS `vw_paquete_costos`;
/*!50001 DROP VIEW IF EXISTS `vw_paquete_costos`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_paquete_costos` AS SELECT 
 1 AS `id_paquete`,
 1 AS `nombre_paquete`,
 1 AS `precio_venta`,
 1 AS `costo_total_paquete`,
 1 AS `ganancia_bruta`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `vw_paquete_costos`
--

/*!50001 DROP VIEW IF EXISTS `vw_paquete_costos`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_paquete_costos` AS select `p`.`id_paquete` AS `id_paquete`,`p`.`nombre` AS `nombre_paquete`,`p`.`precio_venta` AS `precio_venta`,coalesce(sum(`sp`.`costo`),0) AS `costo_total_paquete`,(`p`.`precio_venta` - coalesce(sum(`sp`.`costo`),0)) AS `ganancia_bruta` from (`paquete_turistico` `p` left join `servicio_paquete` `sp` on((`p`.`id_paquete` = `sp`.`id_paquete`))) group by `p`.`id_paquete`,`p`.`nombre`,`p`.`precio_venta` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-13 12:42:46
