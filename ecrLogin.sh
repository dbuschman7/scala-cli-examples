#!/bin/sh 
set -ex 

aws ecr get-login --region ${AWS_REGION} --no-include-email  | docker login -u AWS --password-stdin ${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com

# DONE 
