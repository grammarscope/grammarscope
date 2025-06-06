/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse

interface HasIndex {
    val ith: Int
}

interface HasIndices {
    val lowIndex: Int
    val highIndex: Int
}
