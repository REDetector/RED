# [RED](http://redetector.github.io)

RED(RNA Editing Detector) is a program to detect and visualize RNA editing events at genomic scale using next-generation sequencing data. RNA editing is one
of the post- or co-transcriptional processes with modification of RNA nucleotides from their genome-encoded sequence. In human, RNA editing event occurs
mostly by deamination of adenosine to inosine (A-to-I) conversion through ADAR enzymes.

![image](https://raw.githubusercontent.com/REDetector/redetector.github.io/master/img/fig.1.1.png)

## Features

The main characteristics of RED are:

1. Do Denovo or Non-Denovo(DNA-RNA) detection of RNA editing, while the former just uses RNA VCF file and the latter uses both RNA and DNA VCF file;
2. Visualization of annotated genome with Reference Genes from UCSC and Reference Sequence from fasta file;
3. Visualization of imported mapped raw data (BAM file) with genome scale and chromosome scale;
4. Fast RNA editing detection speed with MySQL database;                    
5. Visualization of editing sites with each step’s filter;
6. Eight RNA editing site’s filters to filter out the most possible spurious sites.
  - Quality Control Filter (contain quality filter and coverage filter);
  - Editing Type Filter
  - Splice Junction Filter;
  - Known SNP Filter;
  - Repeated Regions Filter;
  - DNA-RNA Filter;
  - Fisher's Exact Test Filter;
  - Likelihood Ratio Test Filter.

To get started, check out <http://redetector.github.io>!

## Table of contents

 - [Quick start](#quick-start)
 - [Documentation](#documentation)
 - [Contributing](#contributing)
 - [Bugs and feature requests](#bugs-and-feature-requests)
 - [Creators](#creators)
 - [Copyright and license](#copyright-and-license)

## Quick start

Before letting RED work properly, the following software or program are **required**:

- Java Runtime Environment (tested on jdk 1.6.0_43 or later for Windows)
- MySQL Database Management System (tested on MySQL 5.6.19 or later for Windows)
- R Environment (tested on R 3.1.1 or later for Windows) 

**MySQL Database can be installed in a remote server, see [Prerequisites](http://redetector.github.io/#section1.2) for more detail.**

Two quick start options for our software are available:

- [Download the latest release](https://github.com/REDetector/RED/archive/master.zip).           
- [Download jar package](https://github.com/REDetector/redetector.github.io/raw/master/RED.jar).
- Clone the repo: `git clone https://github.com/REDetector/RED.git`.

### What's included

Within the download you'll find the following directories and files, logically grouping common assets.
You'll see something like this:

```
./    
├──lib
├──red
.gitignore
LICENSE.txt
README.md
Version.txt
```
### How to Work with the Source

#### Intellij IDEA

File->New->Project from Existing Sources...

- Select File or Directory to Import
- Create project from existing sources
- Enter Project Name
- Mark All, Next
- Tick `lib` libraries, Next
- Tick `RED` modueles, Next
- Select Project SDK (Default JDK: jdk1.6.0_43)
- Finish

#### Eclipse

File->New->Java Project

- Enter Project Name: RED
- Untick the `Use default location` and set location to this project, Next
- Finish.


## Documentation

Test data for RED is available [here](https://www.dropbox.com/sh/q8ld5ii635v41e8/AABB7GsOsQO1MlzCN0PsINwVa?dl=0).

RED's documentation, included in this repo in the root directory, is built with [Jekyll](http://jekyllrb.com) and publicly hosted on GitHub Pages at
<http://redetector.github.io>. The docs can also be run locally.

### Contents of documentation

The documentation contains following sections:

- [Introduction](http://redetector.github.io/#chapter1)
- [Getting Started](http://redetector.github.io/#chapter2)
- [Visualization](http://redetector.github.io/#chapter3)
- [Filters](http://redetector.github.io/#chapter4)
- [Reports](http://redetector.github.io/#chapter5)
- [Saving Data](http://redetector.github.io/#chapter6)
- [Configuration](http://redetector.github.io/#chapter7)
- [Reporting Bugs](http://redetector.github.io/#chapter8)
- [Credits](http://redetector.github.io/#chapter9)

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
- email: `echo "c2FtLmx4aW5nQGdtYWlsLmNvbQ==" | base64 -D`

**Di Wu**

- <https://github.com/herbiguncle>
- email: <wubupt@foxmail.com>

## Copyright and license

Code and documentation copyright 2013-2014 BUPT/CQMU. 

RED is a free software, and you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
Foundation; either version 3 of the License, or (at your option) any later version .

SeqMonk & Integrative Genome Viewer (IGV)

We thank [SeqMonk](http://www.bioinformatics.babraham.ac.uk/projects/seqmonk/) and [IGV](http://www.broadinstitute.org/igv/). The framework of the GUI in RED is
 based on SeqMonk, whose GUI is very brief and operating efficiency is fairly high. Meantime, the genome annotation data (mainly referred to gene.txt/gene.gtf) in Feature Track is obtained from genome server of IGV when there is no genome file in the local host.

