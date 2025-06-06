/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#ifndef IFACE_HP_H
#define IFACE_HP_H

#include <string>
#include <vector>

// opaque interface that return protobuf sentences

long
sni_load_h(const char *model, bool is_frozen);

void
sni_unload_h(long handle);

int
sni_version();


std::string
sni_parse_hp(long handle, const char *text);

void
sni_parse_hp(long handle, const std::vector<std::string> &texts, std::vector<std::string> &parsed_sentences);


void
sni_split_parse_hp(long handle, const char *text, std::vector<std::string> &split_parsed_sentences);

void
sni_split_parse_hp(long handle, const std::vector<std::string> &texts, std::vector<std::string> &split_parsed_sentences);


std::string
sni_segment_hp(long handle, const char *text);

void
sni_segment_hp(long handle, const std::vector<std::string> &texts, std::vector<std::string> &segmented_sentences);

#endif
