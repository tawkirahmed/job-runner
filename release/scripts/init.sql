CREATE DATABASE job_runner;

USE job_runner;

CREATE TABLE `jobs` (
  `JOB_ID` int(11) NOT NULL AUTO_INCREMENT,
  `JOB_NAME` varchar(512) NOT NULL,
  `STATUS` int(11) NOT NULL,
  `LAST_RUN_TIME` date DEFAULT NULL,
  `RUN_TIME` bigint(20) DEFAULT NULL,
  `MINIMUM_DATA_OUTPUT_SIZE` bigint(20) DEFAULT NULL,
  `MAXIMUM_DATA_OUTPUT_SIZE` bigint(20) DEFAULT NULL,
  `EXPECTED_DURATION` bigint(20) DEFAULT NULL,
  `LAST_EXECUTION_ID` text,
  `LAST_DATA_OUTPUT_SIZE` bigint(20) DEFAULT NULL,
  `LAST_DURATION` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`JOB_ID`),
  KEY `JOB_NAME_IDX` (`JOB_NAME`)
);

CREATE TABLE `executables` (
  `EXECUTABLE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SCRIPT` longtext NOT NULL,
  `JOB_ID` int(11) NOT NULL,
  PRIMARY KEY (`EXECUTABLE_ID`),
  KEY `JOB_FK` (`JOB_ID`),
  CONSTRAINT `JOB_FK` FOREIGN KEY (`JOB_ID`) REFERENCES `jobs` (`JOB_ID`) ON DELETE CASCADE
);

CREATE TABLE `job_dependencies` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `JOB_ID` int(11) NOT NULL,
  `DEPENDANT_JOB_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `D_JOB_FK` (`DEPENDANT_JOB_ID`),
  CONSTRAINT `D_JOB_FK` FOREIGN KEY (`DEPENDANT_JOB_ID`) REFERENCES `jobs` (`JOB_ID`) ON DELETE CASCADE
);

CREATE TABLE `job_executions` (
  `ID` text NOT NULL,
  `JOB_ID` int(11) NOT NULL,
  `EXECUTABLE_ID` int(11) NOT NULL,
  `STATUS` int(11) NOT NULL,
  KEY `EXECUTABLE_FK` (`EXECUTABLE_ID`),
  CONSTRAINT `EXECUTABLE_FK` FOREIGN KEY (`EXECUTABLE_ID`) REFERENCES `executables` (`EXECUTABLE_ID`) ON DELETE CASCADE
);

CREATE TABLE `job_watchers` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `JOB_ID` int(11) NOT NULL,
  `NAME` varchar(512) NOT NULL,
  `EMAIL` varchar(512) NOT NULL,
  PRIMARY KEY (`ID`)
);
