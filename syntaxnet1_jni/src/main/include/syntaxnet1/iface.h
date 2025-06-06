/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#include <string>
#include <iostream>
#include <exception>

namespace sni {
    class Dragnn;
}

namespace syntaxnet {
    class Sentence;
}

sni::Dragnn *
SNIload(const char *model_path);

void
SNIinfer(sni::Dragnn *dragnn, const char *text, syntaxnet::Sentence *&parsed_sentence);

void
SNIinfer(sni::Dragnn *dragnn, const char *texts[], int n, syntaxnet::Sentence *parsed_sentences[]);

void
SNIunload(sni::Dragnn *dragnn);

int
SNIversion();
