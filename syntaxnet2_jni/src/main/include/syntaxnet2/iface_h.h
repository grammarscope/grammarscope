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
sni_load_h(const char *model);

void
sni_unload_h(long handle);

int
sni_version();


void
sni_parse_h(long handle, const char *text, sentence_t &parsed_sentence);

void
sni_parse_h(long handle, const char *texts[], int n, sentence_t parsed_sentences[]);

void
sni_parse_h(long handle, const std::vector<std::string> &texts, std::vector<sentence_t> &parsed_sentences);


void
sni_split_parse_h(long handle, const char *text, std::vector<sentence_t> &split_parsed_sentences);

void
sni_split_parse_h(long handle, const char *texts[], int n, std::vector<sentence_t> &split_parsed_sentences);

void
sni_split_parse_h(long handle, const std::vector<std::string> &texts, std::vector<sentence_t> &split_parsed_sentences);


void
sni_segment_h(long handle, const char *text, sentence_t &segmented_sentence);

void
sni_segment_h(long handle, const char *texts[], int n, sentence_t segmented_sentences[]);

void
sni_segment_h(long handle, const std::vector<std::string> &texts, std::vector<sentence_t> &segmented_sentences);

#endif
