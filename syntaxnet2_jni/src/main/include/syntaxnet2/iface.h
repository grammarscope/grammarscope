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
sni_load(const char *model_path);

void
sni_unload(sni::Dragnn *dragnn);

int
sni_version();


syntaxnet::Sentence *
sni_parse(sni::Dragnn *dragnn, const char *text);

void
sni_parse(sni::Dragnn *dragnn, const char *texts[], int n, syntaxnet::Sentence *parsed_sentences[]);

void
sni_parse(sni::Dragnn *dragnn, const std::vector<std::string> &texts, std::vector<syntaxnet::Sentence> &parsed_sentences);


syntaxnet::Sentence *
sni_segment(sni::Dragnn *dragnn, const char *text);

void
sni_segment(sni::Dragnn *dragnn, const char *texts[], int n, syntaxnet::Sentence *segmented_sentences[]);

void
sni_segment(sni::Dragnn *dragnn, const std::vector<std::string> &texts, std::vector<syntaxnet::Sentence> &segmented_sentences);
