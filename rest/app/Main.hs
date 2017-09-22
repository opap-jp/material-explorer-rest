{-# LANGUAGE DeriveGeneric #-}
{-# LANGUAGE OverloadedStrings #-}
module Main where

import Web.Spock (spock, runSpock, SpockM, SpockAction, get, post, json, text)
import Web.Spock.Config (SpockCfg, PoolOrConn(PCNoDatabase), defaultSpockCfg)

import Data.Aeson (object, ToJSON, FromJSON, (.=), toJSON)
import Data.Monoid ((<>))
import Data.Text (Text, pack)
import GHC.Generics (Generic)

import Lib

newtype Person = Person
  { name :: Text
  } deriving (Generic, Show)

instance ToJSON Person
instance FromJSON Person

type Api = SpockM () () () ()

type ApiAction a = SpockAction () () () a

main :: IO ()
main = do
  spockCfg <- defaultSpockCfg () PCNoDatabase ()
  runSpock 8080 (spock spockCfg app)

app :: Api
app = get "person" $ json Person { name = "祝園アカネ" }
