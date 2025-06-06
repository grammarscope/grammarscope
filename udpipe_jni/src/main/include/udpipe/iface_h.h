/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#ifndef IFACE_H_H
#define IFACE_H_H

#include <string>
#include <map>
#include <vector>

typedef std::map<std::string, std::string> token_t;
typedef std::vector<token_t> sentence_t;

// opaque interface

long
udpipe_load_h(const char *model);

void
udpipe_unload_h(long handle);

int
udpipe_version();


void
udpipe_parse_h(long handle, const std::string &text, sentence_t &parsed_sentence);

void
udpipe_parse_h(long handle, const std::vector<std::string> &texts, std::vector<sentence_t> &parsed_sentences);

#endif
