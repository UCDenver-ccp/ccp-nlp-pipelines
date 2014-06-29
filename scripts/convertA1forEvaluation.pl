#!/usr/bin/perl
use strict;

# Input:
#     T1	go_term 6 26	Pachytene Checkpoint
#     N1	Reference T1 GO:0051598	
#     T2	go_term 95 103	Synapsis
#     N2	Reference T2 GO:0007129	
# Output:
#     GO:0051598    Pachytene Checkpoint    6   26
#     GP:0007129    Synapsis    95  103

my $inputA1dir = shift;
my $outputDir = shift;

opendir(DH, $inputA1dir);
my @files = readdir(DH);
closedir(DH);

foreach my $file (@files) {
    # Only using the a1 files
    if($file =~ /\.a1/) {
        my %annotations = ();
        
        my $inputFile = $inputA1dir."/".$file;
        print "Processing document: $inputFile\n";
         
        open(IN, $inputFile) or die "Can't open '$inputFile': $!";
        my $line;
        my $singleLine;
        while($line = <IN>) {
            chomp $line;
            
            if($line =~ m/^T[0-9]+/) {
                my @entity = split(/\t/,$line);
                my @spans = split(/ /,$entity[1]);
                $singleLine = $entity[2]."\t".$spans[1]."\t".$spans[2];
                $entity[0] =~ s/T//;
                $annotations{$entity[0]} = $singleLine;
            } else {
                my @reference = split(/\t/,$line);
                my @spans = split(/ /,$reference[1]);
                $reference[0] =~ s/N//;
                $singleLine = $annotations{$reference[0]};
                $singleLine = $spans[2]."\t".$singleLine;
                $annotations{$reference[0]} = $singleLine;
            }
        }
        close IN;
        
        $file =~ s/\.a1/\.txt/g;
        my $outputFile = $outputDir."/".$file;
        open(OUT, ">$outputFile");
        
        for my $key ( sort {$a<=>$b} keys %annotations) {
           print OUT "$annotations{$key}\n";
        }
    
    }
}
