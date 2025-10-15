FROM ubuntu:latest
LABEL authors="Robinson"

ENTRYPOINT ["top", "-b"]