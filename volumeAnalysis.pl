#Pranav Batra
#V1.0

use strict;
use warnings;
use feature 'say';

#Map RCID to road name
my %rcid=();
open(my $fh, '<', "NYHeader_easier.csv") or die "could not open file";
while (my $line = <$fh>) {
chomp $line;
$line =~ tr/"//d;
my @field = split(/,/, $line);
if($field[4] ne ""){ #&&$field[8] eq "NEW YORK"
$rcid{$field[0]}=$field[4]."\t".$field[8]."\t".$field[9];}
}
open(my $out, ">", "rcid.tsv") or die("COULD NOT OPEN");
foreach my $key (sort keys %rcid )
{
say $out $key."\t".$rcid{$key};
}
close($out);


#Generate summary statistics by RCID and hour
my @files=<VOLS/VOL_2001.csv VOLS/VOL_2002.csv VOLS/VOL_2003.csv VOLS/VOL_2004.csv VOLS/VOL_2005.csv VOLS/VOL_2006.csv VOLS/VOL_2007.csv VOLS/VOL_2008.csv VOLS/VOL_2009.csv VOLS/VOL_2010.csv VOLS/VOL_2011.csv>; #qw
my $fh;
my $line;
my %rc; #cluster
my %c; #count
foreach my $i(0..$#files)
{
open($fh,"<",$files[$i]) or die("COULD NOT OPEN ".$files[$i]);
$line=readline($fh);
while($line=readline($fh))
{
chomp $line;
$line =~ tr/"//d;
my @spl=split(/,/, $line); #fields
my @tmp=split(" ",$spl[1]); #hour
$spl[0]=$spl[0]."\t".$tmp[1]; #rcid \t hour
if(!defined $rc{$spl[0]})
{
$rc{$spl[0]}=0;
$c{$spl[0]}=0;
}
$rc{$spl[0]}+=$spl[4];
$c{$spl[0]}++;
if($.%1000000==0){say $.;} #progress
}
close($fh);
}
open($fh,">","rcid_hour_avg_volume.txt") or die("COULD NOT OPEN ");
foreach my $i(sort keys %rc)
{
say $fh $i."\t".$rc{$i}."\t".$c{$i}."\t".$rc{$i}/$c{$i};
}
close($fh);
