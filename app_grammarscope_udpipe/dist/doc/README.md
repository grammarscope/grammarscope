![GrammarScope](logo.png)

# GrammarScope UDPipe

## What it does

Parses sentences and analyses their syntactic structures (in the form of labelled dependencies). Based on [UDPipe](http://ufal.mff.cuni.cz/udpipe) machine learning framework.

## Screencast

* [screencast (graph mode)](https://youtu.be/zmOHILBI6NU)
* [screeencast (text mode)](https://youtu.be/yQQJCgIbG1M)

## Key words

Keys: UDPipe, dependency parser, ML, Machine learning, NLU, natural language understanding, AI, neural network, Universal dependencies.

## How to load you own models

A script is provided [here](https://sourceforge.net/projects/grammarscope-udpipe/files/tools/) that packs the data from UDPipe-provided models

### Layout

* _pack-models.sh_
* __models__
    * chinese-gsd-ud-2.3-181115.udpipe
    * czech-cac-ud-2.3-181115.udpipe
    * czech-pdt-ud-2.3-181115.udpipe
    * english-ewt-ud-2.3-181115.udpipe
    * english-gum-ud-2.3-181115.udpipe
    * ...
* __sentences__ (samples, one sentence per line)
    * Chinese
    * Czech
    * English
    * French
    * ...
* __out-models__ (created)
    * __download__ (created)
        * content (created as a TOC of downloadable files)
        * Chinese-gsd.zip (created, packed model)
        * Chinese-gsd.zip.md5 (created, checksum of previous)
        * Czech-cac.zip
        * Czech-cac.zip.md5
        * Czech-pdt.zip
        * Czech-pdt.zip.md5
        * English-ewt.zip
        * English-ewt.zip.md5
        * ...
    * chinese-gsd (created, the unzipped content)
    * czech-cac
    * czech-pdt
    * english-ewt
    * ...

### Zipped pack

The packed model file should contain:

* model.udpipe (actual model)
* samples (sample sentences)
* model (meta data)
* Urdu (language, name varies)
* Urdu-udtb (language model, name varies)
* md5sum.txt (md5 check sum of files)

### How to download

Run local webserver in the _download_ directory that contains the packages and the _content_ file:
> python3 -m http.server 1313

In the app, change Settings | Download | Model source to http://somehost:1313 where _somehost_ is the name of the host that stores the packages.

In the app menu, choose Model | Download

The list of available files should appear in a dialog box. Choose one. Proceed to download.

When downloading completes successfully, press Deploy.
