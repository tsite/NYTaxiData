#Pranav Batra
#V1.0

use strict;
use warnings;
use feature 'say';
use Data::Dumper;

open(my $fh, '<', "map.tsv") or die "could not open file";
open(my $out, '>', "mapM.tsv") or die "could not open file";

my %type;
my %t2;

while (my $line = <$fh>) {
chomp $line;
my @field = split(/	/, $line); #\t
if(@field==4){} #$field[0]<=$max or $max=$field[0];}
else{
$type{$field[3]}++;
$t2{$field[3]."\t".$field[2]}++;
}
}
close($fh);
while ( (my $key,my $value) = each %type )
{
  say $out $key."\t".$value; #disconnected component distribution
}
close($out);
open($out, '>', "mapM2.tsv") or die "could not open file";
while ( (my $key,my $value) = each %t2 )
{
  say $out $key."\t".$value; #road type distribution
}