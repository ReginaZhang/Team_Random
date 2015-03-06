{-# LANGUAGE OverloadedStrings #-}

import Web.Scotty

import Data.Text.Lazy as T
import Control.Monad.IO.Class
import System.Random as R

main = scotty 8081 $ do
  get "/" $ do
    rnd <- liftIO $ getStdRandom $ randomR (1,10000 :: Int)
    html $ T.pack $ "Hello Team " ++  (show rnd)
