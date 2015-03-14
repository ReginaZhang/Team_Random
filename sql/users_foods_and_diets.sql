use HealthDB;

create table if not exists Address(
  AddressId int NOT NULL AUTO_INCREMENT,
  Content text,
  PRIMARY KEY (AddressId)
)ENGINE=INNODB;

create table if not exists User(
  UserId int NOT NULL AUTO_INCREMENT,
  UserName int NOT NULL,
  AddressId int NOT NULL,
  Email varchar(255) NOT NULL,
  PRIMARY KEY (UserId),
  FOREIGN KEY (AddressId) 
    REFERENCES Address(AddressId)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
)ENGINE=INNODB;

create table if not exists Food(
  FoodId int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (FoodId)
)ENGINE=INNODB;

create table if not exists Diet(
  DietId int NOT NULL AUTO_INCREMENT,
  UserId int NOT NULL,
  PRIMARY KEY (DietId),
  FOREIGN KEY (UserId)
    REFERENCES User(UserId)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
)ENGINE=INNODB;

create table if not exists DietItem(
  FoodId int NOT NULL,
  DietId int NOT NULL,
  PRIMARY KEY (FoodId, DietId),
  FOREIGN KEY (FoodId)
    REFERENCES Food(FoodId)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  FOREIGN KEY (DietId)
    REFERENCES Diet(DietId)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
)ENGINE=INNODB;
