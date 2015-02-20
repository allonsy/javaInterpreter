#!/bin/bash
gcc ReadLine.c -lreadline -I/usr/lib/jvm/java-8-openjdk/include -I/usr/lib/jvm/java-8-openjdk/include/linux --shared -o libReadline.so -fPIC
