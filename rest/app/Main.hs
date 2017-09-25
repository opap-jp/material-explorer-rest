{-# LANGUAGE DeriveGeneric #-}
{-# LANGUAGE OverloadedStrings #-}
module Main where

import qualified Web.Spock as Spock
import Web.Spock (get, post, json, text)
import qualified Web.Spock.Config as Config
import Data.Aeson (ToJSON, FromJSON, (.=), toJSON)
import Data.Monoid ((<>))
import Data.Text (Text, pack)
import GHC.Generics (Generic)

import Lib

newtype Person = Person
  { name :: Text
  } deriving (Generic, Show)

instance ToJSON Person
instance FromJSON Person

type Api = Spock.SpockM () () () ()

type ApiAction a = Spock.SpockAction () () () a

main :: IO ()
main = do
  spockCfg <- Config.defaultSpockCfg () Config.PCNoDatabase ()
  Spock.runSpock 8080 (Spock.spock spockCfg app)

addCors :: Spock.ActionCtxT b (Spock.WebStateM conn sess st) b
addCors = do
  context <- Spock.getContext
  Spock.setHeader "Access-Control-Allow-Origin" "*"
  pure context

app :: Api
app = Spock.prehook addCors routes

routes :: Api
routes =
  get "person" $ json Person { name = "祝園アカネ" }
