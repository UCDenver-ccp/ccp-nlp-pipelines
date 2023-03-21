FROM amazoncorretto:8

RUN yum update -y \
  && yum install -y \
  git \
  less \
  vim \
  maven \
  wget \
  tar \
  && yum clean all

# set up dev user
ARG UID=1000
ARG GID=1000
RUN groupadd -o -g $GID dev
RUN useradd -m -u $UID -g $GID -s /bin/bash dev

# install CRAFT resources
WORKDIR /home/dev
RUN git clone https://github.com/UCDenver-ccp/ccp-nlp-pipelines.git ./ccp-nlp-pipelines.git
WORKDIR /home/dev/ccp-nlp-pipelines.git
RUN mvn clean install
WORKDIR /home/dev
COPY entrypoint.sh empty.owl /home/dev
RUN chmod 755 /home/dev/entrypoint.sh
RUN mkdir input && mkdir output && mkdir dict
# install some further dependencies
RUN mvn -f /home/dev/ccp-nlp-pipelines.git/nlp-pipelines-conceptmapper/pom.xml exec:java -Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder" -Dexec.args="/home/dev/input /home/dev/output CL /home/dev/empty.owl /home/dev/dict false"


ENV MAVEN_OPTS="-Xmx6G"

ENTRYPOINT  [ "/home/dev/entrypoint.sh" ]
### To Build the image
# docker build -t ucdenverccp/nlp-pipelines:0.5.4 .


### To annotate text
# 1. create a local directory that contains plain text files to be annotated: /path/to/txt
# 2. create a local directory where the resulting annotation files wil be stored: /path/to/annot
# 3. create a local directory where the concept mapper dictionaries can be stored: /path/to/dict
# 4. download the ontology that will be used for annotation: /path/to/ontology
# 3. then run the following:
#
# docker run --rm -v /path/to/txt:/home/dev/input -v /path/to/annot:/home/dev/output -v /path/to/dict:/home/dev/dict -v /path/to/ontology:/home/dev/ontology.file nlp-pipelines:0.5.4
#
# 4. After running, text annotation files should be in /path/to/annot