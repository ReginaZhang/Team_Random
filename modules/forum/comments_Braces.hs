{-# LANGUAGE OverloadedStrings, TemplateHaskell #-}

import Web.Scotty

import Data.Text.Lazy as T
import Data.Aeson as A hiding (json)
import Data.Aeson.TH as ATH
import Control.Monad.IO.Class
import System.Random as R

import Database.HDBC as DB
import Database.HDBC.PostgreSQL (connectPostgreSQL)

data Comment = Comment {commentId :: Int, text :: Text, userId :: Int}
$(ATH.deriveJSON ATH.defaultOptions ''Comment)

main = do
  {
  res <- createCommentTable;
  scotty 8081 $ do
    {
    get "/child_comments/:parentId" $ do
       {
         parentId <- param "parentId";
         comments <- liftIO $ getChildComments parentId;
         json $ A.toJSON $ fmap commentToJson comments;
      };
    
    get "/test" $ do
      {
        rnd <- liftIO $ getStdRandom $ randomR (1,10000 :: Int);
        html $ T.pack $ "Hello Team " ++  (show rnd);
      };
    };
  };

commentToJson :: [DB.SqlValue] -> Comment
commentToJson sqlRows = Comment { commentId = DB.fromSql $ sqlRows!!0,
                                  Main.text = DB.fromSql $ sqlRows!!3,
                                  userId = DB.fromSql $ sqlRows!!4}
                        
getChildComments :: Int -> IO [[DB.SqlValue]]
getChildComments parentId = do
  {
    c <- connectPostgreSQL "host=127.0.0.1 dbname=healthdb user=someUser password=somePassword";
    comments <- DB.quickQuery' c "SELECT * from Comment where ParentId = ?" [DB.toSql parentId];
    DB.disconnect c;
    return comments;
  }

createCommentTable = do
  {
    c <- connectPostgreSQL "host=127.0.0.1 dbname=healthdb user=jonathan password=ILuvDicks";
    res <- DB.run c "create table if not exists Comment (\
\CommentId int NOT NULL,\
\ParentId int,\
\QuestionId int NOT NULL,\
\Text text NOT NULL,\
\UserId int NOT NULL,\
\PRIMARY KEY (CommentId)\
\)" [];
    DB.commit c;
    DB.disconnect c;
    return res;
  }
