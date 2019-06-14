# Igor 
This repository contains the sources of igor, a tool for managing 'continuous workarounds'. Workarounds for every-day problems 
exist everywhere, and some of them exist longer than expected (or desired). This tool tries to provide a place to organize 
data-centric workarounds in a centralized and reliable way. 

[![Build Status](https://travis-ci.com/arassec/igor.svg?branch=master)](https://travis-ci.com/arassec/igor)

# Status
Igor is an application to configure generic ['Extract, Transform, Load'](https://en.wikipedia.org/wiki/Extract,_transform,_load)
processes. The web-gui can be used to configure those processes. The current focus lies on file processing, e.g. 'copy a file 
from one source (FTP/SSH/WEB) to a destination (FTP/SSH/WEB)'.

The software is still in an early stage. There are a handful of use-cases around file processing that can be solved right now. At 
the moment, the main focus of work lies on testing and documentation.

In the future, databases, messaging systems or other interfaces could be added to extract, transform and load the data. 

# Installation
The application uses Spring-Boot and is currently delivered standalone (fat) JAR. In order to compile igor at least Java 11 is 
required. As datastore an embedded h2 database ist used. It will automatically be generated upon first start in the current 
working directory. 

In order to try out igor the following steps can be performed:

