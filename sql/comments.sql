use HealthDB;


create table if not exist Comment (
       CommentId int NOT NULL AUTO_INCREMENT,
       ParentId int,
       QuestionId int NOT NULL,
       Text text NOT NULL,
       UserId int NOT NULL,
       PRIMARY KEY (CommentId)
       )ENGINE=INNODB;
