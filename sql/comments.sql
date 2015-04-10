use HealthDB;


create table if not exists Comment(
       CommentId int NOT NULL AUTO_INCREMENT,
       ParentId int,
       QuestionId int NOT NULL,
       Text text NOT NULL,
       UserId int NOT NULL,
       PRIMARY KEY (CommentId)
       )ENGINE=INNODB;

create table if not exists FlagType(
       FlagId int NOT NULL AUTO_INCREMENT,
       FlagName VARCHAR(255),
       PRIMARY KEY (FlagId)
       )ENGINE=INNODB;

create table if not exists CommentFlag(
       FlagId int NOT NULL,
       CommentId int NOT NULL,
       UserId int NOT NULL,
       FOREIGN KEY (FlagId) 
              REFERENCES FlagType(FlagId)
	      ON UPDATE CASCADE
    	      ON DELETE RESTRICT,
       FOREIGN KEY (CommentId) 
              REFERENCES Comment(CommentId)
	      ON UPDATE CASCADE
    	      ON DELETE RESTRICT,
       FOREIGN KEY (UserId) 
              REFERENCES User(UserId)
	      ON UPDATE CASCADE
    	      ON DELETE RESTRICT
       )ENGINE=INNODB;
