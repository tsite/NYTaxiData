#Pranav Batra
#V1.0 - graph analysis

use strict;
use warnings;
use feature 'say';
no warnings 'recursion';
use Data::Dumper;

my @node;
my @used;
my @edge;
my @eall;
my @road;
my @point;
open(my $fh, '<', "map.tsv") or die "could not open file";
while (my $line = <$fh>) {
chomp $line;
my @field = split(/	/, $line); #\t
if(@field==4){$node[$field[0]]=$field[0];$used[$field[0]]=0;@{$edge[$field[0]]}=();@{$eall[$field[0]]}=();@{$road[$field[0]]}=();
@{$point[$field[0]]}=($field[1],$field[2]);} #$field[0]<=$max or $max=$field[0];}
else{
push(@{$road[$field[0]]},$field[3]);#store roads on first point, w.o. loss of generality
push(@{$eall[$field[0]]},$field[1]);push(@{$eall[$field[1]]},$field[0]);
if($field[2]!=-1){push(@{$edge[$field[0]]},$field[1]);}if($field[2]!=1){push(@{$edge[$field[1]]},$field[0]);}} #generate edge list
}
close($fh);

#DFS to find disconnected components
my $c=0;
sub DFS
{
my $i=$_[0];
$used[$i]=1;
foreach my $j(@{$eall[$i]}){$node[$j]=$c;$used[$j] or DFS($j);} #passes assertion ($node[$j]==$j||$node[$j]==$c) or die("ASSERTION FAILED");
}
for(my $i=0;$i<@node;$i++)
{
if($used[$i]==0){DFS($i);$c++;}
}

#Print out results to a file
#open($fh,'>','mapD.tsv');
#for(my $i=0;$i<@node;$i++){say $fh $i."\t".$node[$i];}
#close($fh);

#Summary results
my @sum=();
my @rsum=();
my @cord=();
my %hist=();
my %rhist=();
my %chist=();
for(my $i=0;$i<$c-1;$i++){$sum[$i]=0;@{$cord[$i]}=(1000,-1000,1000,-1000);}
my $in=0;
foreach my $i(@node){$sum[$i]++;
foreach my $j(@{$road[$in]}){exists($rsum[$i]{$j}) or $rsum[$i]{$j}=0;$rsum[$i]{$j}++;}
unless(exists($cord[$i])){
$cord[$i][0]=$point[$in][0];
$cord[$i][2]=$point[$in][1];
$cord[$i][1]=$point[$in][0];
$cord[$i][3]=$point[$in][1];
}
$cord[$i][0]<$point[$in][0] or $cord[$i][0]=$point[$in][0];
$cord[$i][2]<$point[$in][1] or $cord[$i][2]=$point[$in][1];
$cord[$i][1]>$point[$in][0] or $cord[$i][1]=$point[$in][0];
$cord[$i][3]>$point[$in][1] or $cord[$i][3]=$point[$in][1];
$in++;}

foreach my $i(@sum){exists($hist{$i}) or $hist{$i}=0; $hist{$i}++;} #redundant
while(my($i,$k)=each(@sum)){foreach my $j(keys %{$rsum[$i]}){exists($rhist{$k}{$j}) or $rhist{$k}{$j}=0;$rhist{$k}{$j}+=$rsum[$i]{$j};}
exists($chist{$k}) or @{$chist{$k}}=@{$cord[$i]};
$chist{$k}[0]<$cord[$i][0] or $chist{$k}[0]=$cord[$i][0];
$chist{$k}[2]<$cord[$i][2] or $chist{$k}[2]=$cord[$i][2];
$chist{$k}[1]>$cord[$i][1] or $chist{$k}[1]=$cord[$i][1];
$chist{$k}[3]>$cord[$i][3] or $chist{$k}[3]=$cord[$i][3];
}
#print Dumper(%rhist);
my @inf=(	#can mess around with this later to get meaningful results, possibly.
#"motorway",
#"primary",
#"secondary",
#"tertiary",
#"motorway_link",
#"service",
#"road",
#"residential",
#"primary_link",
#"secondary_link",
#"tertiary_link",
#"trunk",
#"trunk_link",
#"living_street"
);
open($fh,'>','mapD.tsv');
foreach my $i(sort {$b <=> $a} keys %hist)
{
my $info="";
foreach my $j(@inf){$info.="\t".($rhist{$i}{$j} or 0)};
say $fh $i."\t".$hist{$i};#."\t".join("\t",@{$chist{$i}}).$info;
}
close($fh);
#Future steps: can look for strongly connected subsets of each weakly-connected road network.