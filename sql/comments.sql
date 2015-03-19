use HealthDB;


create table if not exists Comment (
       CommentId int NOT NULL,
       ParentId int,
       QuestionId int NOT NULL,
       Text text NOT NULL,
       UserId int NOT NULL,
       PRIMARY KEY (CommentId)
       )ENGINE=INNODB;
