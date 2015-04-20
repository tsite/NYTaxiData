"C:\Program Files\Java\jdk1.8.0_20\bin\javac" -cp "C:\Users\pranav\Desktop\course2\proj\ignacioarnaldo-OpenStreetMap2Graph-f85f926\ignacioarnaldo-OpenStreetMap2Graph-f85f926\lib\*" graph\*.java main\*.java parsejsonmaps\*.java roads\*.java
java -cp ".;C:\Users\pranav\Desktop\course2\proj\ignacioarnaldo-OpenStreetMap2Graph-f85f926\ignacioarnaldo-OpenStreetMap2Graph-f85f926\lib\*" main/ParseManager new-york_new-york-roads.geojson highway -100 100 -100 100 > map.tsv
REM -74.0969467163086 -73.8339614868164 40.70016219564594 40.85147526676901 > map.tsv
pause