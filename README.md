# ccp nlp-pipelines

Colorado Computational Pharmacology, University of Colorado School of Medicine
June 26, 2014

The pipelines presented in this project were developed to produce concept 
annotations from any biomedical ontology on any collection of text documents and 
also to evaluate any annotations produced by automatic annotators on the [Colorado 
Richly Annotation Full-Text (CRAFT) Corpus](http://bionlp-corpora.sourceforge.net/CRAFT/). 

These pipelines are derived from those used in the evaluation and analysis of 
three different automatic annotators on the CRAFT corpus, as seen in [Funk et al. 
2014](http://www.ncbi.nlm.nih.gov/pubmed/24571547) (citation below). 
This work found that [Concept Mapper](https://uima.apache.org/sandbox.html#concept.mapper.annotator), 
a UIMA dictionary-based annotator, was the best performer on a majority of the ontologies.

> Funk C, Baumgartner W, Garcia B, Roeder C, Bada M, Cohen KB, Hunter LE & Verspoor 
> K (2014). Large-scale biomedical concept recognition: an evaluation of current 
> automatic annotators and their parameters. BMC Bioinformatics, 15, 59.

## Development
This project follows the Git-Flow approach to branching as originally described [here](http://nvie.com/posts/a-successful-git-branching-model/). 
To facilitate the Git-Flow branching approach, this project makes use of the [jgitflow-maven-plugin](https://bitbucket.org/atlassian/jgit-flow) as described [here](http://george-stathis.com/2013/11/09/painless-maven-project-releases-with-maven-gitflow-plugin/).

Code in the [master branch](https://github.com/UCDenver-ccp/common/tree/master) reflects the latest release of this library. Code in the [development](https://github.com/UCDenver-ccp/common/tree/development) branch contains the most up-to-date version of this project.

## Maven signature
```xml
<dependency>
    <groupId>edu.ucdenver.ccp</groupId>
    <artifactId>nlp-pipelines-conceptmapper</artifactId>
    <version>0.5.3</version>
</dependency>
<dependency>
    <groupId>edu.ucdenver.ccp</groupId>
    <artifactId>nlp-pipelines-evaluation</artifactId>
    <version>0.5.3</version>
</dependency>

<repository>
    <id>bionlp-sourceforge</id>
    <url>http://svn.code.sf.net/p/bionlp/code/repo/</url>
</repository>
```

##Producing concept annotations from the command line
The pipeline to produce concept annotations is located in the 
_edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder_ class. To produce 
annotations from a directory of text files, do the following:

1. Install MAVEN via the instructions found at http://maven.apache.org/download.cgi.

2. Change directory to the nlp-pipelines project.

3. Compile, test, and package the source use the following command:

> mvn clean install

4. Annotate text files in a directory using the following command:

> mvn -f nlp-pipelines-conceptmapper/pom.xml exec:java 
> -Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder" 
> -Dexec.args="\<InputDir\> \<OutputDir\> \<ontology\> \<OBOfile\> \<DictDir\> \<CleanDictFlag\>"

The parameters required are as follows:

* \<InputDir\> - Directory of text files for input. The text files should be named by 
the identifier of the article and have a file extension of ".txt". The text files 
can be in ASCII or UTF-8 encoding but must be plain text (i.e. not XML). It is possible to use a 
different collection reader with XML or other input but it requires editing the source.

* \<OutputDir\> - Directory where annotations will be written. Output is one ".a1" 
file per input text file in the BioNLP/Brat format. More information about the output format can be seen [here](http://2011.bionlp-st.org/home/file-formats). Simply, the format stores one annotation per line (and an extra line for the ontology concept reference). Annotation lines take the form of 

> annotation\_id [tab] annotation\_type span\_start\_offset  span\_end\_offset [tab] covered\_text 

The lines beginning with N 
capture the ontology term reference and take the form of

>normalization\_id [tab] Reference annotation\_id ontology\_concept\_id 

Serialization of two sample Gene Ontology annotations using the BioNLP format are shown below:

> T2 go\_term 232 244	pigmentation  
> N2 Reference T2 GO:0043473  
> T7 go\_term 1841 1862	embryonic development  
> N7 Reference T7 GO:0009790

* \<ontology\> - Specifies which ontology concepts are used for annotation. If an ontology evaluated in Funk et al. 2014 is selected, then the best parameter combinations discovered in the published evaluation are used. The 
ontologies with specific parameter combinations are: 
  * CHEBI - Chemical Entities of 
Biological Interest
  * SO - Sequence Ontology
  * GO - Gene Ontology
    * GO_MF - GO 
Molecular Function
    * GO_CC - GO Cellular Component
    * GO_BP - GO Biological Process
  * PR - Protein Ontology
  * CL - Cell Ontology
  * NCBI_TAXON - NCBI Taxonomy

If the 
specific ontology of interestes is not listed please use "OBO" as the value for this parameter.

* \<OBOfile\> - The OBO or OWL ontology file. A large list of biomedical ontologies can be 
found at http://www.obofoundry.org/.

* \<DictDir\> - The directory where the Concept Mapper dictionary will be written. 

* \<CleanDictFlag\> - If true, previously build dictionary files are deleted and 
the dictionary is recreated prior to use. If false, previously used dictionary 
files are used if they are available.


##Evaluating annotations against CRAFT:
The pipeline to evaluate annotations again the CRAFT corpus is located in the
_edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.CraftRunGenericComparison_ class. 
It compares normalized concepts based on their text spans. Prior annotations must 
be used, this pipeline is only for evaluation. Annotations can be obtained from 
any automatic annotator and converted to the correct format, or obtained from 
the previously discussed pipeline and converted via the script to the correct 
format. The following command will run the evaluation pipeline:

> mvn -f nlp-pipelines-evaluation/pom.xml exec:java 
-Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.
CraftRunGenericComparison" -Dexec.args="\<ontology\> \<AnnotationDir\> \<outputFile 
\<comparisonSpanMatcher\> OPTIONAL:\<scoreCutoff\>"

The parameters are as follows:

* \<ontology\> - Specifies which ontology the annotations are from. It must be one of 
the ontologies annotated in CRAFT. The ontologies are:    
  * CHEBI - Chemical Entities of Biological Interest
  * SO - Sequence Ontology
  * GO_MF - GO Molecular Function 
  * GO_CC - GO Cellular Component
  * GO_BP - GO Biological Process
  * PR - Protein Ontology
  * CL - Cell Ontology
  * NCBI_TAXON - NCBI Taxonomy
  * EG - Entrez Gene

* \<AnnotationDir\> - The directory where the annotations are placed. The directory 
should contain on annotation file for each of the 67 CRAFT documents following 
the naming convention of "PMID.txt". Each concept annotation should be on a 
single tab delimited line, should be formatted as follows:

> ConceptID [tab] covered\_text [tab] span\_start\_offset [tab] span\_end\_offset [tab] OPTIONAL:score

* \<outputFile\> - The output filename that will be produced. It lists True Positives 
(TP), False Positives (FP), and False Negatives (FN) for each document along with 
how overall performance in terms of Precision (P), Recall (R), and F1-measure 
(F).

* \<comparisonSpanMatcher\> - Specifies how to compare the annotations based on their 
text span. Results will differ depending on which parameter is chosen. The 
options are as follows:
  * STRICT - Text spans must match exactly. This is the strictest option.
  * OVERLAP - Some part of the annotation and gold standard must overlap. This is         
    the loosest option.
  * SHARED_START - The annotation and gold standard must share beginning text 
    span, but not the end. 
  * SHARED_END - The annotation and gold standard must share end text span, but 
    not the beginning. 
  * SHARED_START_OR_END - The annotation and gold standard must share either the 
    beginning or end, but not both. 

* OPTIONAL:\<scoreCutoff\> - The cutoff parameter is optional. If your annotation 
produces a confidence score it can be included and annotations can be filtered 
based off of it. Only annotations with scores >= cutoff parameter provided are 
used.

##Converting A1 files evaluations:
A script in nlp-pipelines/scripts named "convertA1forEvaluation.pl" converts 
BioNLP/Brat format annotations produced by the concept annotation pipeline to the 
required format for evaluation against CRAFT. The correct usage is:

>./convertA1forEvaluation.pl \<inputA1Dir\> \<outputDir\>

where:
* \<inputA1Dir\> - is the directory that contains the A1 annotation files for every 
corresponding article in CRAFT.

* \<outputDir\> - the directory where output will be written.


##License
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
developed by Christopher Funk \<Christopher.Funk@ucdenver.edu\>, William A 
Baumgartner Jr \<William.Baumgartner@ucdenver.edu\>, Christophe Roeder, and Karin 
Verspoor \<Karin.Verspoor@ucdenver.edu\> and should reference the following 
publication:

\>Funk C, Baumgartner W, Garcia B, Roeder C, Bada M, Cohen KB, Hunter LE & Verspoor 
K (2014). Large-scale biomedical concept recognition: an evaluation of current 
automatic annotators and their parameters. BMC Bioinformatics, 15, 59.
