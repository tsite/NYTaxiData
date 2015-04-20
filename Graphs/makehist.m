%Pranav Batra
%V1.0
%Tested on matlab only, but should still work in octave.

%Note: the flowcharts were made in gliffy (online web app).
%The second flowchart (flow3.png) and its associated histogram (abar.png) were not included in the final report due to lack of space.
%rate2.gif was created using makeagif.com from the images in the folder movie.

function makehist
global lon;
global lat;
global lat1;
global lat2;
global lon1;
global lon2;
lon1=-79.765001;
lon2=-71.790277;
lat2=45.011668;
lat1=40.494443;
lon=lon1+(lon2-lon1)/200:(lon2-lon1)/100:lon2-(lon2-lon1)/200;
lat=lat1+(lat2-lat1)/200:(lat2-lat1)/100:lat2-(lat2-lat1)/200;
distsub; %geographic distance distribution
timesub; %time duration distribution
ocsub; %overcharge distribution
ratesub %rate distribution
dtrsub; %actual distance distribution
errsub; %grid error distribution
statsub %bar charts of errors at each step
todsub; %hour of day / day of week distribution
gridsub; %geographic distribution
moviesub; %distribution of rates
mov2sub; %distribution of counts (helps visualize the data a bit)
mapsub; %disconnected components scatter plot
trafficsub; %hour of day / day of week traffic volume distribution
disp([lon(1:2) lat(1:2)]); %dimensions of grid rectangle
end


%center third (x), bottom sixth (y), 2:1 ratio.
function mov2sub %taking the right-bottom portion of the grid & blowing it up.
global lon;
global lat;
rate=reshape(importdata('grid.txt'),24*7,100,100);
count=reshape(importdata('gridd.txt'),24*7,100,100);
rate=permute(rate(:,61:90,1:15),[3 2 1]);
count=permute(count(:,61:90,1:15),[3 2 1]);
m=max(log10(count(:)));
disp(m);
for i=1:24*7
h=figure('visible','off');
imagesc([lon(61) lon(90)],[lat(1) lat(15)],log10(count(:,:,i)));
set(gca,'YDir','normal');
ylabel(colorbar(),'Log(count)');
caxis([0,m]);
xlabel('Longitude');
ylabel('Latitude');
saveas(h,strcat('mov2/c',int2str(i),'.png'));
end
end

%center third (x), bottom sixth (y), 2:1 ratio.
function moviesub %taking the right-bottom portion of the grid, blowing it up.
global lon;
global lat;
rate=reshape(importdata('grid.txt'),24*7,100,100);
count=reshape(importdata('gridd.txt'),24*7,100,100);
rate=permute(rate(:,61:90,1:15),[3 2 1]);
count=permute(count(:,61:90,1:15),[3 2 1]);
m=60;
rate(rate(:)>m)=m; %rate cutoff
%disp(m);
for i=1:24*7
h=figure('visible','off');
imagesc([lon(61) lon(90)],[lat(1) lat(15)],rate(:,:,i));
set(gca,'YDir','normal');
ylabel(colorbar(),'Speed (mph)');
caxis([0,m]);
xlabel('Longitude');
ylabel('Latitude');
title(strcat('T=',int2str(i)));
saveas(h,strcat('movie/time',int2str(i),'.png'));
end
end

function trafficsub
c=reshape(importdata('../VOLS/hcount.txt'),24,7);
d=reshape(importdata('../VOLS/hist.txt'),24,7);
h=figure();
imagesc(d./c);
ylabel(colorbar(),'Average traffic volume (per hourly detector)');
xlabel('Day of week');
ylabel('Hour of day');
saveas(h,'trgrid.png');
h=figure();
imagesc(c);
ylabel(colorbar(),'Number of hourly detectors');
xlabel('Day of week');
ylabel('Hour of day');
saveas(h,'tr2grid.png');
end

function statsub
stats=importdata('stats.txt');
%file 7 additional stats (from error.log)
excessive_distance=15;
excessive_time=25;
negative_time=115;

mean_error=stats(1);
mean_square_error=stats(2);
lines=stats(3);
err_assert=stats(12); %should be equal to err(8)+err(9)+err(10)+err(1)
err=[]; %map processing (lint.java) errors
err(1)=stats(5); %missing field
err(2)=stats(6); %0 in field (distance, time, lat, long)
err(3)=stats(10); %geographic distance zero (aka same lat long coords)
err(4)=stats(9); %outside of geographic range
err(5)=stats(7); %distance more than geographic distance (aka mistyped coordinate)
err(6)=stats(8); %time interval does not match end time-start time
err(7)=stats(11); %speed cutoff at 100 mph
err(8)=excessive_distance;
err(9)=excessive_time;
err(10)=negative_time;
err(11)=stats(4); %skip if no overcharge found ONLY for overcharge analysis
err(12)=err_assert-(err(8)+err(9)+err(10)+err(1)); %unknown errors
err_shortest_path=stats(4); %shortest path
min_lat=stats(13);
max_lat=stats(14);
min_lon=stats(15);
max_lon=stats(16);
a=[]; %A* (AStar.java) errors
a(1)=stats(22); %missing data
a(2)=stats(20); %in lat/long range of map?
a(3)=stats(19); %start/end node not found
a(4)=stats(18); %start node=end node
a(5)=stats(21); %astar not found
all_lines=stats(17)+sum(a);
time=stats(23); %total time taken by lint
%start with a bar chart of error types for main map processing (lint)
h=figure();
bar(err);
set(gca,'YScale','log');
xlabel('Step');
ylabel('Excluded lines');
saveas(h,'ebar.png');
%next bar chart of error times for a* search (AStar)
h=figure();
bar(a);
set(gca,'YScale','log');
xlabel('Step');
ylabel('Excluded lines');
saveas(h,'abar.png');
end

function mapsub
mapM2=importdata('mapM3.tsv'); %Bar chart of frequency of road types
mapD=importdata('mapD.tsv'); %Scatter plot of distribution of weakly-connected components
h=figure('visible','off');
loglog(mapD(:,2),mapD(:,1),'.','MarkerSize',5);
xlabel('Number of disconnected components');
ylabel('Nodes per component');
saveas(h,'components.png');
h=figure('visible','off');
[s,I]=sort(-sum(mapM2.data.'));
disp(mapM2.textdata(I));
m=mapM2.data(I,1)>mapM2.data(I,2);
hold on
h0=bar(-s);
h1=bar(m.*mapM2.data(I,1),'FaceColor',[0,0.7,0.7]);
h2=bar(mapM2.data(I,2),'FaceColor','red');
bar((1-m).*mapM2.data(I,1),'FaceColor',[0,0.7,0.7]);
h3=bar(mapM2.data(I,3),'FaceColor','green');
hold off
%set(gca,'XtickLabel',mapM2.textdata); %the text labels overflow...
set(gca,'YScale','log')
legend([h0,h1,h2,h3],'total','bidirectional','one-way (normal)','one-way (reverse)');
saveas(h,'roads.png');
end

function todsub
tod=reshape(importdata('tgrid.txt'),24,7);
h=figure('visible','off');
imagesc(tod);
ylabel(colorbar(),'Count');
xlabel('Day of week');
ylabel('Hour of day');
saveas(h,'tgrid.png');
end

function gridsub
global lon;
global lat;
global lat1;
global lat2;
global lon1;
global lon2;
grid=reshape(importdata('ggridd.txt'),100,100).';
h=figure('visible','off');
imagesc([lon1 lon2],[lat1 lat2],log10(grid));
set(gca,'YDir','normal');
caxis([-4,8]);
ylabel(colorbar(),'Log(count)');
xlabel('Longitude');
ylabel('Latitude');
saveas(h,'dgrid.png');
end

function errsub
err=prune(importdata('merd.txt').',.0001/2:.0001:1.5-.0001/2);
h=figure('visible','off');
loglog(err(1,:),err(2,:),'.','MarkerSize',5);
xlabel('Distance from intersections (miles)');
ylabel('PDF (count)');
saveas(h,'epdf.png');
%pdf plot (loglog)
h=figure('visible','off');
semilogy(err(1,:),err(2,:),'.','MarkerSize',5);
xlabel('Distance from intersections (miles)');
ylabel('PDF (count)');
saveas(h,'epdf2.png');
%pdf2 (semilogy)
h=figure('visible','off');
semilogx(err(1,:),err(3,:)/err(3,end),'.','MarkerSize',5);
xlabel('Distance from intersections (miles)');
ylabel('CDF');
saveas(h,'ecdf.png');
%cdf plot
end

function dtrsub
dtr=prune(importdata('dtr.txt').',0:.01:150-.01);
sel2=10:10:length(dtr); %nearest .1 miles
sel=setdiff(1:length(dtr),sel2);
h=figure('visible','off');
p1=loglog(dtr(1,sel2),dtr(2,sel2),'.','MarkerSize',5);
hold on;
p2=loglog(dtr(1,sel),dtr(2,sel),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
legend([p1,p2],'hundredth of mile=0','hundredth of mile!=0');
xlabel('Actual distance (miles)');
ylabel('PDF (count)');
saveas(h,'apdf.png');
h=figure('visible','off');
semilogx(dtr(1,:),dtr(3,:)/dtr(3,end),'.','MarkerSize',5);
xlabel('Actual distance (miles)');
ylabel('CDF');
saveas(h,'acdf.png');
%pdf2
h=figure('visible','off');
p1=semilogy(dtr(1,sel2),dtr(2,sel2),'.','MarkerSize',5);
hold on;
p2=semilogy(dtr(1,sel),dtr(2,sel),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
legend([p1,p2],'hundredth of mile=0','hundredth of mile!=0');
xlabel('Actual distance (miles)');
ylabel('PDF (count)');
saveas(h,'apdf2.png');
end


function ratesub
rate=prune(importdata('grate.txt').',.01/2:.01:150-.01/2);
h=figure('visible','off');
loglog(rate(1,:),rate(2,:),'.','MarkerSize',5);
xlabel('Rate (mph)');
ylabel('PDF (count)');
saveas(h,'rpdf.png');
%pdf plot
h=figure('visible','off');
semilogy(rate(1,:),rate(2,:),'.','MarkerSize',5);
xlabel('Rate (mph)');
ylabel('PDF (count)');
saveas(h,'rpdf2.png');
%pdf2
h=figure('visible','off');
scatter(rate(1,:),rate(3,:)/rate(3,end),9,'.');
xlabel('Rate (mph)');
ylabel('CDF');
saveas(h,'rcdf.png');
%cdf plot
end

function ocsub
oc=prune(importdata('overchg.txt').',.01*(1/2-1000):.01:150-.01*(1/2+1000));
oc1=oc(:,oc(1,:)>0);
oc2=oc(:,oc(1,:)<0);
%PDF-1
h=figure('visible','off');
p1=loglog(oc1(1,:),oc1(2,:),'.','MarkerSize',5);
hold on;
p2=loglog(-oc2(1,:),oc2(2,:),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
legend([p1,p2],'positive overcharge','negative overcharge');
xlabel('Overcharge (miles)');
ylabel('PDF (count)');
saveas(h,'opdf.png');
%PDF-2
h=figure('visible','off');
semilogy(oc(1,:),oc(2,:),'.','MarkerSize',5);
xlabel('Overcharge (miles)');
ylabel('PDF (count)');
saveas(h,'opdf2.png');
%CDF
h=figure('visible','off');
p1=semilogx(oc1(1,:),oc1(3,:)/oc(3,end),'.','MarkerSize',5);
hold on;
p2=semilogx(-oc2(1,:),oc2(3,:)/oc(3,end),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
xlabel('Overcharge (miles)');
ylabel('CDF');
legend([p1,p2],'positive overcharge','negative overcharge');
saveas(h,'ocdf.png');
end

function timesub
time=prune(importdata('timehist.txt').',0:15000-1);
sel2=(61-time(1,1)):60:length(time); %zero seconds
sel=setdiff(1:length(time),sel2);
h=figure('visible','off');
p1=loglog(time(1,sel2),time(2,sel2),'.','MarkerSize',5);
hold on;
p2=loglog(time(1,sel),time(2,sel),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
legend([p1,p2],'s=0','s!=0');
xlabel('Trip duration (seconds)');
ylabel('PDF (count)');
saveas(h,'tpdf.png');
h=figure('visible','off');
semilogx(time(1,:),time(3,:)/time(3,end),'.','MarkerSize',5);
xlabel('Trip duration (seconds)');
ylabel('CDF');
saveas(h,'tcdf.png');
%pdf2
h=figure('visible','off');
p1=semilogy(time(1,sel2),time(2,sel2),'.','MarkerSize',5);
hold on;
p2=semilogy(time(1,sel),time(2,sel),'.','MarkerSize',5,'Color',[0,.5,0]);
hold off;
legend([p1,p2],'s=0','s!=0');
xlabel('Trip duration (seconds)');
ylabel('PDF (count)');
saveas(h,'tpdf2.png');
end

function distsub
dist=prune(importdata('dhist.txt').',.01/2:.01:150-.01/2);
h=figure('visible','off');
loglog(dist(1,:),dist(2,:),'.','MarkerSize',5);
xlabel('Geographic distance (miles)');
ylabel('PDF (count)');
saveas(h,'dpdf.png');
%pdf plot
h=figure('visible','off');
semilogy(dist(1,:),dist(2,:),'.','MarkerSize',5);
xlabel('Geographic distance (miles)');
ylabel('PDF (count)');
saveas(h,'dpdf2.png');
%pdf2
h=figure('visible','off');
semilogx(dist(1,:),dist(3,:)/dist(3,end),'.','MarkerSize',5);
xlabel('Geographic distance (miles)');
ylabel('CDF');
saveas(h,'dcdf.png');
%cdf plot
end

function [binspdfcdf]=prune(pdf,bins) %helper function to prune data & generate CDF function
if isequal(size(pdf),size(bins))==0,disp('error: pdf, bins neq');end %assertion
s=find(pdf,1,'first');
e=find(pdf,1,'last');
binspdfcdf=[bins(s:e);pdf(s:e);pdf(s:e)]; %trim
for i=2:(e-s+1)
    binspdfcdf(3,i)=binspdfcdf(3,i)+binspdfcdf(3,i-1);
end
end