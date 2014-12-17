# [RED](http://redetector.github.io)

RED(RNA Editing Detector) is a program to detect and visualize RNA editing events at genomic scale using next-generation sequencing data. RNA editing is one
of  the post- or co-transcriptional processes with modification of RNA nucleotides from their genome-encoded sequence. In human, RNA editing event occurs
mostly  by deamination of adenosine to inosine (A-to-I) conversion through ADAR enzymes.

![image](https://raw.githubusercontent.com/REDetector/redetector.github.io/master/img/fig.1.1.png)

The main characteristics of this software are:

1. Do Denovo or Non-Denovo detection of RNA editing, while the former just uses RNA vcf file and the latter uses both RNA vcf file and DNA vcf file;
2. Visualization of annotated genome with Reference Genes from UCSC and Reference Sequence from fasta file;
3. Visualization of imported mapped raw data (bam/sam etc.) with genome scale and chromosome scale;
4. Faster RNA editing detection speed with MySQL database;
5. Eight RNA editing site’s filters to filter out the most possible spurious sites.
  - Basic Filter (contain quality filter and coverage filter);
  - Editing Type Filter
  - Splice Junction Filter;
  - Known SNP Filter;
  - Repeated Regions Filter;
  - DNA-RNA Filter;
  - Fisher's Exact Test Filter;
  - Likelihood Ratio Test Filter;
6. Visualization of editing sites with each step’s filter.

To get started, check out <http://redetector.github.io>!

## Table of contents

 - [Quick start](#quick-start)
 - [Documentation](#documentation)
 - [Contributing](#contributing)
 - [Bugs and feature requests](#bugs-and-feature-requests)
 - [Creators](#creators)
 - [Copyright and license](#copyright-and-license)

## Quick start

Before making RED work properly, the following software or programs are **demanded**:

- Java Runtime Environment (jdk 1.6.0_43 or later)
- MySQL Database Management System (MySQL 5.6.19 or later)
- R Environment (R 3.1.1 or later) 

Two quick start options for our software are available:

- [Download the latest release](https://github.com/REDetector/RED/archive/master.zip).
- Clone the repo: `git clone https://github.com/REDetector/RED.git`.

### What's included

Within the download you'll find the following directories and files, logically grouping common assets and providing both compiled and minified variations.
You'll see something like this:

```
./
├──lib
├──red
.gitignore
LICENSE.txt
README.md
RED.jar
```
## Documentation

Test data for RED is available [here](https://www.dropbox.com/sh/q8ld5ii635v41e8/AABB7GsOsQO1MlzCN0PsINwVa?dl=0).

RED's documentation, included in this repo in the root directory, is built with [Jekyll](http://jekyllrb.com) and publicly hosted on GitHub Pages at
<http://redetector.github.io>. The docs may also be run locally.

### Contents of documentation

The documentation contains following sections:

- Introduction
- Getting Started
- Visualization
- Filters
- Reports
- Saving Data
- Configuration
- Reporting Bugs
- Credits

Please move to pages at <http://redetector.github.io> for more information.

### Documentation for previous releases

[Previous releases](https://github.com/REDetector/RED/releases) and their documentation are also available for download.

## Contributing

Please read through our [contributing guidelines](https://github.com/REDetector/RED/graphs/contributors).

## Bugs and feature requests

Have a bug or a feature request? Please first read the [issue guidelines](https://github.com/REDetector/RED/issues) and search for existing and closed issues
. If your problem or idea is not addressed yet, please open a [new issue](https://github.com/REDetector/RED/issues/new).

## Creators

**Xing Li**

- <https://github.com/iluhcm>
- email: <sam.lxing@gmail.com>

**Di Wu**

- <https://github.com/herbiguncle>
- email: <wubupt@foxmail.com>

## Copyright and license

Code and documentation copyright 2013-2014 BUPT/CQMU. 

RED is a free software, and you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
Foundation; either version 3 of the License, or (at your option) any later version .

SeqMonk & Integrative Genome Viewer (IGV)

We thank SeqMonk(http://www.bioinformatics.babraham.ac.uk/projects/seqmonk/) and IGV(http://www.broadinstitute.org/igv/). The framework of the GUI in RED is based on SeqMonk, whose GUI is very brief and operating efficiency is fairly high. Meantime, the genome annotation data (mainly referred to gene.txt/gene.gtf) in Feature Track is obtained from genome server of IGV when there is no genome file in the local host.

