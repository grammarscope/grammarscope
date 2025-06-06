/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#ifndef IFACE2_H
#define IFACE2_H

#include <string>
#include <map>
#include <vector>

typedef std::map<std::string, std::string> token_t;
typedef std::vector<token_t> sentence_t;

// opaque interface

long
SNIload_h(const char *model);

void
SNIinfer_h(long handle, const std::string &text, sentence_t &parsed_sentence);

void
SNIinfer_h(long handle, const std::vector<std::string> &texts, sentence_t parsed_sentences[]);

void
SNIunload_h(long handle);

int
SNIversion();

#endif
