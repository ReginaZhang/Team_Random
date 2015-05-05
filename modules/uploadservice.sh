#!/bin/bash

scp -r give* ray@45.56.85.191:
ssh ray@45.56.85.191
cd give*
nohup mvn exec:java -Dexec.mainClass="main.App" -e &