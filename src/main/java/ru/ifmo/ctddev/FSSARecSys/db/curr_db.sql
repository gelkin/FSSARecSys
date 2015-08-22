CREATE TABLE `Dataset` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` TEXT,
  `file` MEDIUMBLOB,
  `taskType` TEXT,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `MetaFeatures` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` TEXT,
  `classPath` TEXT,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `DatasetFeatures` (
  `idDataset` INT NOT NULL,
  `idMf` INT NOT NULL,
  `value` DOUBLE,
  PRIMARY KEY  (`idDataset`,`idMf`)
);

CREATE TABLE `FSAlgorithm` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` TEXT,
  `searchClass` TEXT,
  `evalClass` TEXT,
  `searchOptions` TEXT,
  `evalOptions` TEXT,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `MLAlgorithm` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` TEXT,
  `classPath` TEXT,
  `options` TEXT,
  `taskType` TEXT,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `Metrics` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `idFSAlgo` INT,
  `idDataset` INT,
  `listOfFeatures` MEDIUMBLOB,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `Params` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` TEXT,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `MetricParams` (
  `idParams` INT NOT NULL,
  `idMetric` INT NOT NULL,
  `idMLAlgo` INT NOT NULL,
  `value` DOUBLE,
  PRIMARY KEY  (`idParams`,`idMetric`, `idMLAlgo`)
);


ALTER TABLE `DatasetFeatures` ADD CONSTRAINT `DatasetFeatures_fk1` FOREIGN KEY (`idDataset`) REFERENCES Dataset(`id`);
ALTER TABLE `DatasetFeatures` ADD CONSTRAINT `DatasetFeatures_fk2` FOREIGN KEY (`idMf`) REFERENCES MetaFeatures(`id`);


ALTER TABLE `Metrics` ADD CONSTRAINT `Metrics_fk1` FOREIGN KEY (`idFSAlgo`) REFERENCES FSAlgorithm(`id`);
ALTER TABLE `Metrics` ADD CONSTRAINT `Metrics_fk2` FOREIGN KEY (`idDataset`) REFERENCES Dataset(`id`);

ALTER TABLE `MetricParams` ADD CONSTRAINT `MetricParams_fk1` FOREIGN KEY (`idParams`) REFERENCES Params(`id`);
ALTER TABLE `MetricParams` ADD CONSTRAINT `MetricParams_fk2` FOREIGN KEY (`idMetric`) REFERENCES Metrics(`id`);
ALTER TABLE `MetricParams` ADD CONSTRAINT `MetricParams_fk3` FOREIGN KEY (`idMLAlgo`) REFERENCES MLAlgorithm(`id`)