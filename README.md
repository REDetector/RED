# [RED](http://bellatangtang.github.io)

RED(RNA Editing events Detector) is a program to detect and visualize RNA editing events at genomic scale using next-generation sequencing data. RNA editing is one of the post- or co-transcriptional processes with modification of RNA nucleotides from their genome-encoded sequence. In human, RNA editing event occurs mostly by deamination of adenosine to inosine (A-to-I) conversion through ADAR enzymes. The main characteristics of this software are:

1. Do Denovo or Non-Denovo detection of RNA editing, while the former just uses RNA vcf file and the latter uses both RNA vcf file and DNA vcf file;
2. Visualization of annotated genome with Reference Genes from UCSC and Reference Sequence from fasta file;
3. Visualization of imported mapped raw data (bam/sam etc.) with genome scale and chromosome scale;
4. Faster RNA editing detection speed with MySQL database;
5. Eight RNA editing site’s filters to filter out the most possible spurious sites.
  - Basic Filter (contain quality filter and coverage filter);
  - Comprehensive Filter;
  - dbSNP Filter;
  - pvalue Filter;
  - Repeat Filter;
  - DNA RNA Filter;
  - LLRFilter;
6. Visualization of editing sites with each step’s filter.

To get started, check out <http://bellatangtang.github.io>!

## Table of contents

 - [Quick start](#quick-start)
 - [Documentation](#documentation)
 - [Contributing](#contributing)
 - [Community](#community)
 - [Versioning](#versioning)
 - [Creators](#creators)
 - [Copyright and license](#copyright-and-license)

## Quick start

Before making RED work properly, the following software or programs are **demanded:**

- MySQL (version 5.6 or later);
- R (version 3.1.1 or later).

Two quick start options for our software are available:

- [Download the latest release](https://github.com/twbs/bootstrap/archive/v3.2.0.zip).
- Clone the repo: `git clone https://github.com/bellatangtang/bellatangtang.github.io.git`.

### What's included

Within the download you'll find the following directories and files, logically grouping common assets and providing both compiled and minified variations. You'll see something like this:

```
E:.
├──.idea
├──.settings
├──bin
├──Data
├──lib
├──out
├──red
```

We have a compiled software edition under `/bin` directory, while the source code is under `/red` directory.
For the compiled edition, you can run the software with the following command:`java /bin/com/xl/main/REDApplication.class`
Otherwise, you first should compile the source code with the shell we provided:`sh redcompile.sh`
And run it with:`java /bin/com/xl/main/REDApplication.class`
It is more convenient if there is any IDE installed on your device.


## Bugs and feature requests

Have a bug or a feature request? Please first read the [issue guidelines]( https://github.com/iluhcm/REDetector/issues) and search for existing and closed issues. If your problem or idea is not addressed yet, [please open a new issue]( https://github.com/iluhcm/REDetector/issues/new).

## Documentation

RED's documentation, included in this repo in the root directory, is built with [Jekyll](http://jekyllrb.com) and publicly hosted on GitHub Pages at <http://bellatangtang.github.io/>. The docs may also be run locally.

### Running documentation locally

1. If necessary, [install Jekyll](http://jekyllrb.com/docs/installation) (requires v2.1.x).
  - **Windows users:** Read [this unofficial guide](https://github.com/juthilo/run-jekyll-on-windows/) to get Jekyll up and running without problems.
2. Install the Ruby-based syntax highlighter, [Rouge](https://github.com/jneen/rouge), with `gem install rouge`.
3. From the root `/bootstrap` directory, run `jekyll serve` in the command line.
4. Open <http://localhost:9001> in your browser, and voilà.

### Contents of documentation

The documentation contains following sections:

- Introduction
- Opening a Project
- Visualization
- Filtering
- Reports
- Saving Data
- Configuration
- Troubleshooting

Please move to pages at <http://bellatangtang.github.io/> for more information.

### Documentation for previous releases

[Previous releases]( https://github.com/iluhcm/REDetector/releases) and their documentation are also available for download.

## Contributing

Please read through our [contributing guidelines]( https://github.com/iluhcm/REDetector/graphs/contributors).

## Creators

**Xing Li**

- <https://github.com/iluhcm>
- email: <sam.lxing@gmail.com>

**Di Wu**

- <https://github.com/herbiguncle>
- email: <wubupt@foxmail.com>

## Copyright and license

Code and documentation copyright 2013-2014 BUPT/CQMU. Code released under GPL v3 license or later.
