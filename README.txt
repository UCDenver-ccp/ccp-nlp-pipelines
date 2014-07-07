Colorado Computational Pharmacology, University of Colorado School of Medicine
June 26, 2014

The pipelines presented in this project were developed to produce concept 
annotations from any biomedical ontology on any collection of text documents and 
also to evaluate any annotations produced by automatic annotators on the Colorado 
Richly Annotation Full-Text (CRAFT) Corpus 
(http://bionlp-corpora.sourceforge.net/CRAFT/). 

These pipelines are derived from those used in the evaluation and analysis of 
three different automatic annotators on the CRAFT corpus, as seen in Funk et al. 
2014 (citation below). This work found that Concept Mapper, a UIMA dictionary 
based annotator, 
(https://uima.apache.org/downloads/sandbox/ConceptMapperAnnotatorUserGuide/
ConceptMapperAnnotatorUserGuide.html) was the best performer on a majority of the 
ontologies and is natively UIMA, so the pipeline makes use it as an automatic 
annotator.

Funk C, Baumgartner W, Garcia B, Roeder C, Bada M, Cohen KB, Hunter LE & Verspoor 
K (2014). Large-scale biomedical concept recognition: an evaluation of current 
automatic annotators and their parameters. BMC Bioinformatics, 15, 59.

This directory contains the software developed by Christopher Funk 
<Christopher.Funk@ucdenver.edu>, William A Baumgartner Jr 
<William.Baumgartner@ucdenver.edu>, Christophe Roeder, and Karin Verspoor 
<Karin.Verspoor@unimelb.edu.au>. The pipelines are implemented in the UIMA Java 
framework (https://uima.apache.org/) and are released as open source software to 
the NLP and text mining research communities to be used for research purposes 
only (see below for copyright information).


Installation
---------------------------------------------------------------------------------
Install MAVEN via the instructions found at http://maven.apache.org/download.cgi.

Change directory to the nlp-pipelines project. To compile, test, and package the 
source use the following command:

mvn compile test package

If you use Maven as your build tool, you can add the our pipelines as a 
dependency by adding the following to your pom.xml file:

<dependency>
    <groupId>edu.ucdenver.ccp</groupId>
    <artifactId>nlp-pipelines-conceptmapper</artifactId>
    <version>0.5</version>
</dependency>
<dependency>
    <groupId>edu.ucdenver.ccp</groupId>
    <artifactId>nlp-pipelines-evaluation</artifactId>
    <version>0.5</version>
</dependency>

<repository>
    <id>bionlp-sourceforge</id>
    <url>http://bionlp.svn.sourceforge.net/viewvc/bionlp/repo/</url>
</repository>


Producing concept annotations
---------------------------------------------------------------------------------
The pipeline to produce concept annotations is located in 
edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder.java. To produce 
annotations from a directory of text files use the command in the nlp-pipelines 
direcotry:

mvn -f nlp-pipelines-conceptmapper/pom.xml exec:java 
-Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder" 
-Dexec.args="<InputDir> <OutputDir> <ontology> <OBOfile> <DictDir>"

The parameters required are as follows:
<InputDir> - Directory of text files for input. The text files should be named by 
the identifier of the article and have a file extension of ".txt". The text files 
can be in ASCII or UTF-8 encoding but must be plain text. It is possible to use a 
different collection reader with xml input but it requires editing the source.

<OutputDir> - Directory where annotations will be written. Output is one ".a1" 
file per input text file in the BioNLP/Brat format (further detail explained 
below).

More information about the output format can be seen at 
http://2011.bionlp-st.org/home/file-formats. Simply, the format is "entity# \t 
typeEntity \t spanBegin \t spanEnd \t coveredText". The lines beginning with N 
capture the normalization. Examples from the Gene Ontology can be seen below:
T2	go_term 232 244	pigmentation
N2	Reference T2 GO:0043473	
T7	go_term 1841 1862	embryonic development
N7	Reference T7 GO:0009790	

<ontology> - Specifies which ontology concepts are from. If one of the ontologies 
evaluated in Funk et al. 2014 the best parameter combinations are used. The 
ontologies with specific parameter combinations are: CHEBI - Chemical Entities of 
Biological Interest, SO - Sequence Ontology, GO - Gene Ontology, GO_MF - GO 
Molecular Function, GO_CC - GO Cellular Component, GO_BP - GO Biological Process, 
PR - Protein Ontology, CL - Cell Ontology, NCBI_TAXON - NCBI Taxonomy. If the 
specific ontology of interestes is not listed please use OBO.

<OBOfile> - The obo ontology file. A large list of biomedical ontologies can be 
found at http://www.obofoundry.org/.

<DictDir> - The directory where the Concept Mapper dictioary will be written. 


Evaluating annotations against CRAFT:
---------------------------------------------------------------------------------
The pipeline to evaluate annotations again CRAFT is located in 
edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.CraftRunGenericComparison.java. 
It compares normalized concepts based on their text spans. Prior annotations must 
be used, this pipeline is only for evaluation. Annotations can be obtained from 
any automatic annotator and convereted to the correct format, or obtained from 
the previously discussed pipeline and converted via the script to the correct 
format. The following command will run the evaluation pipeline:

mvn -f nlp-pipelines-evaluation/pom.xml exec:java 
-Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.
CraftRunGenericComparison" -Dexec.args="<ontology> <AnnotationDir> <outputFile 
<comparisonSpanMatcher> OPTIONAL:<scoreCutoff>"

The parameters are as follows:
<ontology> - Specifies which ontology the annotations are from. It must be one of 
the ontologies annotated in CRAFT. The ontologies are: CHEBI - Chemical Entities 
of Biological Interest, SO - Sequence Ontology, GO_MF - GO Molecular Function, 
GO_CC - GO Cellular Component, GO_BP - GO Biological Process, PR - Protein 
Ontology, CL - Cell Ontology, NCBI_TAXON - NCBI Taxonomy, EG - Entrez Gene.

<AnnotationDir> - The directory where the annotations are placed. The directory 
should contain on annotation file for each of the 67 CRAFT documents following 
the naming convention of "PMID.txt". Each concept annotation should be on a 
single tab delimited line, should be formatted as follows:

"ConceptID \t coveredText \t spanBegin \t spanEnd \t OPTIONAL:score \n"

For example, "CL:0000235	macrophage	5757	5768	756", in CRAFT document 
11532192 an annotation of CL:0000235 begins at offset 5757 and ends at offset 
5768 and has a confidence score of 756. 

<outputFile> - The output filename that will be produced. It lists True Positives 
(TP), False Positives (FP), and False Negatives (FN) for each document along with 
how overall performance in terms of Precision (P), Recall (R), and F1-measure 
(F).

<comparisonSpanMatcher> - Specifies how to compare the annotations based on their 
text span. Results will differ depending on which parameter is chosen. The 
options are as follows:
    STRICT - Text spans must match exactly. This is the strictest option.
    OVERLAP - Some part of the annotation and gold standard must overlap. This is         
    the loosest option.
    SHARED_START - The annotation and gold standard must share beginning text 
    span, but not the end. 
    SHARED_END - The annotation and gold standard must share end text span, but 
    not the beginning. 
    SHARED_START_OR_END - The annotation and gold standard must share either the 
    beginning or end, but not both. 

OPTIONAL:<scoreCutoff> - The cutoff parameter is optional. If your annotation 
produces a confidence score it can be included and annotations will be filtered 
based off of it. Only annotations with scores >= cutoff parameter provided are 
used.



Converting A1 files evaluations:
---------------------------------------------------------------------------------
A script in nlp-pipelines/scripts named "convertA1forEvaluation.pl" converts 
BioNLP/Brat format annotations produced by the concept annotation pipeline to the 
required format for evaluation against CRAFT. The correct usage is:

./convertA1forEvaluation.pl <inputA1Dir> <outputDir>

where
<inputA1Dir> - is the directory that contains the A1 annotation files for every 
corresponding article in CRAFT.

<outputDir> - the directory where annotations will be written.


COPYRIGHT INFO
---------------------------------------------------------------------------------
The software is released under the New BSD license 
(http://www.opensource.org/licenses/bsd-license.php).

Copyright (c) 2014, Regents of the University of Colorado
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this 
    list of conditions and the following disclaimer.
   
  * Redistributions in binary form must reproduce the above copyright notice, 
    this list of conditions and the following disclaimer in the documentation 
    and/or other materials provided with the distribution.
   
  * Neither the name of the University of Colorado nor the names of its 
    contributors may be used to endorse or promote products derived from this 
    software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Any documentation, advertising materials, publications and other materials 
related to redistribution and use must acknowledge that the software was 
developed by Christopher Funk <Christopher.Funk@ucdenver.edu>, William A 
Baumgartner Jr <William.Baumgartner@ucdenver.edu>, Christophe Roeder, and Karin 
Verspoor <Karin.Verspoor@ucdenver.edu> and must refer to the following 
publication:

Funk C, Baumgartner W, Garcia B, Roeder C, Bada M, Cohen KB, Hunter LE & Verspoor 
K (2014). Large-scale biomedical concept recognition: an evaluation of current 
automatic annotators and their parameters. BMC Bioinformatics, 15, 59.