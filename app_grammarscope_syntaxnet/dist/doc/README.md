![GrammarScope](./logo.png)

# GrammarScope SyntaxNet

## What it does

Parses sentences and analyses their syntactic structures (in the form of labelled dependencies). Based on [SyntaxNet](http://ufal.mff.cuni.cz/udpipe) machine learning framework.

## Screencast

* [screencast, graph mode](https://youtu.be/u42kZY-guZc)
* [screeencast, text mode](https://youtu.be/x8f8Ti1YxLM)

## Key words

Keys: SyntaxNet, Tensorflow, dependency parser, ML, Machine learning, NLU, natural language understanding, AI, neural network, Universal dependencies.

## How to load you own models

Scripts are provided [here](https://sourceforge.net/projects/grammarscope/files/tools/) that save, freeze, format, pack the data from SyntaxNet-provided parseysaurus models.

* tools.tar.xz (the scripts)
* syntaxnet_with_tensorflow-0.2-cp27-cp27mu-linux_x86_64.whl (the tensorflow+syntaxnet runtime)

__WARNING: the standard pip syntaxnet-with-tensorflow is an older version and won't do.__

What the scripts do:

* strip unnecessary training data
* rename variable
* transform the model to Tensorflow-standard saved model
* freeze this model
* tweak it so as to add entry points directing to assets directory
* package it

### Layout

* _prepare.sh_
* _reset_models.sh_
* _make_models.sh_
* _make_models.sh_
* _support python files_
* __reference-models__ (copy the training models here)
    * __Chinese__
        * checkpoint
        * parser_spec.textproto
        * tables + maps
        * __segmenter__
            + checkpoint.meta
            + checkpoint.index
            + checkpoint.data-00000-of-00001
            + spec.textproto
            + tables + maps
    * __English-LinES__
    * __English-ParTUT__
    * ...
* __sentences__ (copy your samples here, one sentence per line, one file par language)
    * Chinese
    * English
    * French
    * ...
* __models__ (created by reset_models)
    * __Chinese__
        * checkpoint.meta
        * checkpoint.index
        * checkpoint.data-00000-of-00001
        * parser_spec.textproto
        * tables + maps
        * __segmenter__
            + checkpoint.meta
            + checkpoint.index
            + checkpoint.data-00000-of-00001
            + spec.textproto
            + tables + maps
    * __English-LinES__
    * __English-ParTUT__
    * ...
* __out-models__ (created by make_models)
    * __Chinese__ (created, the unzipped content)
        * __export_conll2017__
        * __frozen__
        * __tweaked__
        * __zipped__
    * __English-LinES__
    * ...
* __download__ (created)
    * content (created as a TOC of downloadable files)
    * Chinese.zip (created, packed model)
    * Chinese.zip.md5 (created, checksum of previous)
    * English-LinES.zip
    * English-LinES.zip.md5
    * ...

Here are the difference steps.

1. __Prepare__. This will install a) system packages needed by tensorflow, b) syntaxnet and a matching tensorflow version as user packages in ~/.local/lib/python2.7/site-packages

> ./prepare.sh

2. __Reset__. Install your models in reference-models and to make a work copy in the models dir, run

> ./reset-models

3. __Make__. Make and package: this will rename the variables, strip training data, freeze, tweak the model

> ./make_models.sh

4. __Distrib__. Make a downloadable distrib

> ./dist_make.sh

### Zipped pack

The packed model file should contain:

* __parser__
    * graph.pb (actual frozen model)
* __segmenter__
    * graph.pb (actual frozen model)
* __assets.extra__ (map and table assets)
    * __resources__
        * __component_0_char_ltsm__
            * __resource_0_word-map__
                * part_0
            * __resource_1_tag-map__
                * part_0
            * __resource_2_tag-to-category__
                * part_0
            * __resource_3_lcword-map__
                * part_0
            * __resource_4_category-map__
                * part_0
            * __resource_5_char-map__
                * part_0
            * __resource_6_char-ngram-map__
                * part_0
            * __resource_7_label-map__
                * part_0
            * __resource_8_prefix-table__
                * part_0
            * __resource_9_suffix-table__
                * part_0
            * __resource_10_known-word-map__
                * part_0
        * __component_0_lookahead__
            * __resource_0_word-map__
                * part_0
            * __resource_1_tag-map__
                * part_0
            * __resource_2_tag-to-category__
                * part_0
            * __resource_3_lcword-map__
                * part_0
            * __resource_4_category-map__
                * part_0
            * __resource_5_char-map__
                * part_0
            * __resource_6_char-ngram-map__
                * part_0
            * __resource_7_label-map__
                * part_0
            * __resource_8_prefix-table__
                * part_0
            * __resource_9_suffix-table__
                * part_0
            * __resource_10_known-word-map__
                * part_0
* samples (sample sentences)
* model (meta data)
* Urdu (language, name varies)
* Urdu-udtb (language model, name varies)
* md5sum.txt (md5 check sum of files)

### How to download

Run local webserver in the _download_ directory that contains the packages and the _content_ file:
> python3 -m http.server 1313

or use _./webserver.sh_

In the app, change Settings | Download | Model source to http://somehost:1313 where _somehost_ is the name of the host that stores the packages.

In the app menu, choose Model | Download

The list of available files should appear in a dialog box. Choose one. Proceed to download.

When downloading completes successfully, press Deploy.
