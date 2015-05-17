use HealthDB;

create table if not exists Address(
  AddressId int NOT NULL AUTO_INCREMENT,
  Content text,
  PRIMARY KEY (AddressId)
)ENGINE=INNODB;

create table if not exists User(
  UserId int NOT NULL AUTO_INCREMENT,
  UserName varchar(255) NOT NULL,
  Password varchar(255) NOT NULL,
  AddressId int NOT NULL,
  Email varchar(255) NOT NULL,
  Weight double NOT NULL,
  Height double NOT NULL,
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
  DietItemId int NOT NULL AUTO_INCREMENT,
  DietId int NOT NULL,
  Ndbno varchar(6) NOT NULL,
  MealType ENUM('B','L','D','O') NOT NULL DEFAULT 'O',
  Weekday  ENUM('Mon','Tue','Wed','Thu','Fri','Sat','Sun','NA') NOT NULL DEFAULT 'NA',
  PRIMARY KEY (DietItemId),
  FOREIGN KEY (Ndbno)
    REFERENCES Food(Ndbno)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  FOREIGN KEY (DietId)
    REFERENCES Diet(DietId)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
)ENGINE=INNODB;
