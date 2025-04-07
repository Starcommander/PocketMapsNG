#!/bin/bash

set -e

##############################################################################
##
##  Starcommander@github.com
##  Pauls script to download and convert (and upload) all maps!
##  The only manual steps:
##  - Import the mapfile-writer
##  - - See also https://github.com/mapsforge/mapsforge-creator
##  - Install: zip git wget curl tee bc xmlstarlet openjdk
##  - Maybe you have to increase MEMORY_USE
##
##  ============= Steps that are executed automatically: ===========
##  Download the osm-maps from:
##  - http://download.geofabrik.de/
##  Import the map:
##  - ./graphhopper.sh import downloadedMap.osm.pbf
##  Download existing map file, or create the map file with osmosis tool:
##  - osmosis --rb file=/tmp/berlin.osm.pbf --mapfile-writer file=/tmp/berlin.map
##  - - See also http://wiki.openstreetmap.org/wiki/Osmosis/Installation
##  Create file city_nodes.txt for limited OfflineSearch (Geocoding)
##  Compress the content of mapdir-gh to mapdir.ghz
##  Copy map and create entry into json list and html file (server mode)
##
##  ============= Workaround to create SwapMemory ===============
##  These lines can be executed when not enough ram-memory exists on system.
##  This may slow down build process, but creates 4GB swap-memory.
##  > dd if=/dev/zero of=/tmp/swapfile1 bs=1024 count=4194304
##  > chown root:root /tmp/swapfile1
##  > chmod 0600 /tmp/swapfile1
##  > mkswap /tmp/swapfile1
##  > swapon /tmp/swapfile1
##
##############################################################################

WORK_DIR="/tmp/graphhopper_1-0/"
HOPPER_REP="https://github.com/graphhopper/graphhopper.git"
HOPPER_TAG="tags/1.0"
GEO_TMP="/tmp/geofabrik-list.txt"
GEO_URL="http://download.geofabrik.de/"
OSM_URL="https://github.com/openstreetmap/osmosis/releases/download/0.49.2/osmosis-0.49.2.tar"
MAP_URL="http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v5/"
MAP_URL_ZIP_ALASKA="http://ftp.gwdg.de/pub/misc/openstreetmap/openandromaps//maps/usa/Alaska.zip"
MAP_DIR="/tmp/graphhopper_1-0/maps-osm/"
MAP_REV="1.0_0"
LINK_BRAZIL=$GEO_URL"south-america/brazil-latest.osm.pbf"
LINK_CHINA=$GEO_URL"asia/china-latest.osm.pbf"
LINK_SPAIN=$GEO_URL"europe/spain-latest.osm.pbf"
CONTINUE="ask"
MEMORY_USE="2048m"
MEMORY_HD="yes"
SERVER_MAPS_REMOTE="" # User@ip using ssh
SERVER_MAPS_DIR_DEFAULT="/var/www/html/maps/maps/"
SERVER_MAPS_DIR_DAYS=180
KEEP_DOUBLE_MAPS="yes"
LOG_FILE="/tmp/pocketmaps-generate.log"

# Tags: VERSION_USED MANIPULATE TODO

print_map_list() # Args europe|europe/germany
{
  curl "$GEO_URL$1.html" > "$GEO_TMP"
  local lineS=$(cat -n "$GEO_TMP" | grep "Sub Regions" | head -n 1 | awk '{ print $1; }')
  local content=$(cat "$GEO_TMP" | tail --lines="+$lineS")
  local hasSpecSubs=$(cat "$GEO_TMP" | grep "Special Sub Regions")
  if [ ! -z "$hasSpecSubs" ]; then
    local lineE=$(cat -n "$GEO_TMP" | grep "Special Sub Regions" | head -n 1 | awk '{ print $1; }')
    local lineE=$(echo "$lineE - $lineS" | bc)
    local content=$(echo "$content" | head --lines="$lineE")
  fi
  echo "$content" | grep -o 'href=".*-latest\.osm\.pbf\"'
}

check_exist() # Args: file|dir
{
  if [ ! -e "$1" ]; then
    echo "Error, path is missing: $1" | tee -a "$LOG_FILE"
    exit 1
  fi
}

check_result() # Args: exitCode jobInfo
{
  if [ "$1" != "0" ]; then
    echo "Exitcode-error on job: $2" | tee -a "$LOG_FILE"
    exit 1
  fi
}

goto_graphhopper_mem()
{
  # MANIPULATE: Changed to use hdd instead of ram memory.
  if [ "$MEMORY_HD" = "yes" ]; then
    sed -i -e "s#^  graph.dataaccess: RAM_STORE\$#  graph.dataaccess: MMAP_STORE#g" config.yml
  else
    sed -i -e "s#^  graph.dataaccess: MMAP_STORE\$#  graph.dataaccess: RAM_STORE#g" config.yml
  fi
}

goto_graphhopper()
{
  export JAVA_OPTS="-Xmx$MEMORY_USE -Xms1000m -server"
  if [ -d "$WORK_DIR/gh" ]; then
    cd "$WORK_DIR/gh"
    goto_graphhopper_mem
    return
  fi
  mkdir -p "$WORK_DIR"
  cd "$WORK_DIR"
  echo "Checking out graphhopper repository, please wait ..."
  # VERSION_USED: graphhopper_1.0
  git clone "$HOPPER_REP" gh
  cd gh
  git checkout "$HOPPER_TAG"
  # MANIPULATE: The default config must be changed, because some flags are missing otherwise.
  # MANIPULATE: Also in PocketMaps: MapHandler.java: "shortest" must be added manually
  cp config-example.yml config.yml
  sed -i -e "s#^  graph.flag_encoders: car\$#  graph.flag_encoders: car,bike,foot#g" config.yml
  sed -i -e "s#^  profiles:\$#  profiles:\n    - name: foot\n      vehicle: foot\n      weighting: fastest#g" config.yml
  sed -i -e "s#^  profiles:\$#  profiles:\n    - name: bike\n      vehicle: bike\n      weighting: fastest#g" config.yml
  sed -i -e "s#^    - profile: car\$#    - profile: car\n    - profile: bike\n    - profile: foot#g" config.yml
  goto_graphhopper_mem
}

goto_osmosis()
{
  local work_tmp_dir="$WORK_DIR/osmosis_tmp"
  export JAVACMD_OPTIONS="-Xmx$MEMORY_USE -server -Djava.io.tmpdir=$work_tmp_dir"
  if [ ! -d "$work_tmp_dir" ]; then
    mkdir "$work_tmp_dir"
  fi
  if [ -d "$WORK_DIR/osmosis" ]; then
    cd "$WORK_DIR/osmosis"
    return
  fi
  mkdir -p "$WORK_DIR/osmosis"
  # VERSION_USED: osmosis-0.49.2.tar and plugin mapsforge-map-writer-master-20171203.163155-186-jar-with-dependencies.jar
  local osm_name=$(basename -s .tar "$OSM_URL")
  wget "$OSM_URL" -O "$WORK_DIR/osmosis/${osm_name}.tar"
  cd "$WORK_DIR/osmosis"
  tar -xf "${osm_name}.tar"
  mv ${osm_name}/* .
  rm ${osm_name}.tar
  rmdir ${osm_name}
  chmod a+x bin/osmosis
}

printCityNodeLine() # args: key value
{
  if [ ! -z "$2" ]; then
    echo "$1=$2"
  fi
}

printCityNode() # args: cityNodes.osm counter/count start_line
{
  local node_start=$3
  local node_length=$(cat "$1" | tail --lines=+$node_start | grep --max-count=1 --line-number "^ *</node" | cut -d':' -s -f 1)
  local node_content=$(cat "$1" | tail --lines=+$node_start | head --lines=$node_length)

  local vLat=$(echo "$node_content" | xmlstarlet sel -t -v "/node/@lat")
  local vLon=$(echo "$node_content" | xmlstarlet sel -t -v "/node/@lon")
  local vName=$(echo "$node_content" | xmlstarlet sel -t -v "/node/tag[@k='name']/@v")
  local vNameEn=$(echo "$node_content" | xmlstarlet sel -t -v "/node/tag[@k='name:en']/@v")
  local vPostal=$(echo "$node_content" | xmlstarlet sel -t -v "/node/tag[@k='post']/@v")

  if [ -n "$vName$vNameEn" -a -n "$vLat" -a -n "$vLon" ]; then
    echo "name=$vName"
    printCityNodeLine "name:en" "$vNameEn"
    printCityNodeLine "post" "$vPostal"
    printCityNodeLine "lat" "$vLat"
    printCityNodeLine "lon" "$vLon"
    echo "CityNode $2: $vName" 1>&2
  fi
}

printCityNodes() # args: cityNodes.osm
{
  local node_start_lines=$(cat "$1" | grep --line-number "^ *<node" | cut -d':' -s -f 1)
  local node_count=$(echo "$node_start_lines" | wc -l)
  local node_up="1"
  for cur_city_node_line in $node_start_lines
  do
    printCityNode "$1" "$node_up/$node_count" "$cur_city_node_line"
    local node_up=$(echo "$node_up + 1" | bc)
  done
  echo "CityNode finish!" 1>&2
}

# Deletes doubles when newest file is at least one hour old.
# TODO: Instead delete files regarding to json list
clear_old_double_files() # args: Path/to/maps_dir
{
  if [ "$KEEP_DOUBLE_MAPS" = "yes" ]; then
    return
  fi
  local cur_tmp_file=$(mktemp)
  if [ ! -z "$SERVER_MAPS_REMOTE" ]; then
    local cur_connect="ssh $SERVER_MAPS_REMOTE"
  else
    local cur_connect="bash -c"
  fi
  $cur_connect "find \"$1\" -name \"*.ghz\"" | xargs --max-args=1 basename | sort -u > "$cur_tmp_file"
  while read line ; do
    if [ -z "$line" ]; then
      continue
    fi
    local map_files=$($cur_connect "find \"$1\" -name \"$line\"" | sort -r)
    local map_files_count=$(echo "$map_files" | wc -l)
    if [ $map_files_count -lt 2 ]; then
      continue
    fi
    local map_file_first=$(echo "$map_files" | head --lines=1)
    local last_mod=$($cur_connect "stat -c '%Y' \"$map_file_first\"")
    local last_mod=$(echo "$(date +%s) - $last_mod" | bc)
    local one_hour="3600"
    if [ $last_mod -lt $one_hour ]; then
      continue
    fi
    $cur_connect "find \"$1\" -name \"$line\" -not -wholename \"$map_file_first\" -delete"
  done < "$cur_tmp_file"
  rm "$cur_tmp_file"
}

import_map_box() # Args: lat1,lon1,lat2,lon2 /abs/path/cont_country.osm.pbf subName
{
  if [ -z "$3" ]; then
    echo "Args input error for map_box!" | tee -a "$LOG_FILE"
    exit 1
  fi
  local target_file=$MAP_DIR$(basename "$2" | sed -e "s#.osm.pbf\$#-$3-latest.osm.pbf#g")
  
  local bbox_top=$(echo "$1" | cut -d',' -s -f 1)
  local bbox_left=$(echo "$1" | cut -d',' -s -f 2)
  local bbox_bottom=$(echo "$1" | cut -d',' -s -f 3)
  local bbox_right=$(echo "$1" | cut -d',' -s -f 4)
  local arg_bbox="--bounding-box top=$bbox_top left=$bbox_left bottom=$bbox_bottom right=$bbox_right"
  
  goto_osmosis
  ./bin/osmosis --rb file="$2" $arg_bbox --write-pbf file="$target_file"
  if [ "$?" != "0" ]; then
    echo "Osmosis returned an error, clearing file."
    rm "$target_file"
  fi

  local continent=$(basename "$2" | cut -d'_' -s -f 1)
  local country=$(basename "$2" | cut -d'_' -s -f 2 | cut -d'.' -s -f 1)
  check_exist "$target_file"
  import_map $(basename "$target_file")
}

import_map() # Args: map_url_rel
{
  if [ ! -z "$SERVER_MAPS_REMOTE" ]; then
    local cur_connect="ssh $SERVER_MAPS_REMOTE"
  else
    local cur_connect="bash -c"
  fi
  mkdir -p "$MAP_DIR"
  if [ -d "$MAP_DIR" ]; then
    local free_space=$(df --output=avail "$MAP_DIR" | sed 1d)
    if [ "$free_space" -lt 10000000 ]; then
      local free_space=$(df -h --output=size "$MAP_DIR" | sed 1d)
      echo "Error, low disk space: $free_space" 1>&2
      echo "Error, low disk space: $free_space" >> "$LOG_FILE"
      echo "Exiting."
      exit 2
    fi
  fi
  if [ ! -z "$SERVER_MAPS_DIR" ]; then
    local free_space=$($cur_connect "df --output=avail \"$SERVER_MAPS_DIR\"" | sed 1d)
    if [ "$free_space" -lt 10000000 ]; then
      echo "Error, low disk space on server dir: $free_space" 1>&2
      echo "Error, low disk space on server dir: $free_space" >> "$LOG_FILE"
      echo "Exiting."
      exit 2
    fi
  fi
  local start_time=$(date +%s)
  local map_file=$(echo "$1" | tr '/' '_')
  local gh_map_name=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$//g')
  local gh_map_dir=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/-gh/g')
  local gh_map_file=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/.map/g')
  local gh_mapfile_path=$(echo "$1" | sed -e 's/-latest.osm.pbf$/.map/g') # WGET source file
  local gh_map_zip=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/.ghz/g')

  if [ -f "$MAP_DIR$gh_map_zip" ]; then
    echo "Allready existing: $gh_map_zip"
    return
  fi
  if [ ! -z "$SERVER_MAPS_DIR" ]; then
    local serv_map_file_new=$($cur_connect "find \"$SERVER_MAPS_DIR\" -name \"$gh_map_zip\" -mtime \"-$SERVER_MAPS_DIR_DAYS\"")
    if [ ! -z "$serv_map_file_new" ]; then
      echo "There is already one actual map on server-dir."
      return
    fi
    clear_old_double_files "$SERVER_MAPS_DIR"
  fi
  if [ ! -f "$MAP_DIR$map_file" ]; then
    wget "$GEO_URL$1" -O "$MAP_DIR$map_file"
  fi
  check_exist "$MAP_DIR$map_file"
  if [ ! -d "$MAP_DIR$gh_map_dir" ]; then
    goto_graphhopper
    ./graphhopper.sh import "$MAP_DIR$map_file"
    if [ "$?" != "0" ]; then
      echo "Graphhopper returned an error!"
    elif [ -d "$MAP_DIR$gh_map_name"-latest.osm-gh ]; then
      mv "$MAP_DIR$gh_map_name"-latest.osm-gh "$MAP_DIR$gh_map_dir"
    fi
  fi
  check_exist "$MAP_DIR$gh_map_dir/nodes_ch_car"
  if [ ! -f "$MAP_DIR$gh_map_dir/$gh_map_file" ]; then
    goto_osmosis
    if wget -q --spider "$MAP_URL/$gh_mapfile_path" ; then
      #TODO: Check timestamp of source-file.
      wget "$MAP_URL/$gh_mapfile_path" -O "$MAP_DIR$gh_map_dir/$gh_map_file"
    elif [[ "$gh_map_file" == *"alaska.map" ]]; then
      #TODO: Check timestamp of source-file.
      local unzip_dir=$(mktemp -d)
      wget -q --spider "$MAP_URL_ZIP_ALASKA" && \
      wget "$MAP_URL_ZIP_ALASKA" -O "$unzip_dir/file.zip" && \
      unzip "$unzip_dir/file.zip" -d "$unzip_dir/" && \
      mv "$unzip_dir/"*.map "$MAP_DIR$gh_map_dir/$gh_map_file" && \
      rm -r "$unzip_dir"
    else
      ./bin/osmosis --rb file="$MAP_DIR$map_file" --mapfile-writer type=hd file="$MAP_DIR$gh_map_dir/$gh_map_file"
      if [ "$?" != "0" ]; then
        echo "Osmosis returned an error, clearing file."
        rm "$MAP_DIR$gh_map_dir/$gh_map_file"
      fi
    fi
    check_exist "$MAP_DIR$gh_map_dir/$gh_map_file"
    ./bin/osmosis --rb file="$MAP_DIR$map_file" \
                  --tf reject-relations \
                  --tf reject-ways \
                  --tf accept-nodes place=city,town,village \
                  --write-xml "$MAP_DIR$gh_map_dir/cityNodes.osm"
    if [ "$?" != "0" ]; then
      echo "Osmosis for cityNodes returned an error, clearing file." | tee -a "$LOG_FILE"
      echo "" > "$MAP_DIR$gh_map_dir/cityNodes.osm"
    else
      echo "Generate City nodes for $gh_map_dir" >> "$LOG_FILE"
      printCityNodes "$MAP_DIR$gh_map_dir/cityNodes.osm" > "$MAP_DIR$gh_map_dir/city_nodes.txt"
    fi
    rm "$MAP_DIR$gh_map_dir/cityNodes.osm"
  fi
  if [ ! -f "$MAP_DIR$gh_map_zip" ]; then
    cd "$MAP_DIR$gh_map_dir"
    echo "map-version: $MAP_REV" > versions.yml
    echo "hopper-tag: $HOPPER_TAG" >> versions.yml
    zip -r "$WORK_DIR$gh_map_zip" *
    rm -r -f "$MAP_DIR/$gh_map_dir" # Cleanup
    rm "$MAP_DIR/$map_file" # Cleanup
    mv "$WORK_DIR$gh_map_zip" "$MAP_DIR"
  fi
  check_exist "$MAP_DIR$gh_map_zip"
  local ghz_size=$(du -h "$MAP_DIR$gh_map_zip" | awk '{ print $1 }')
  local ghz_time=$(date +%Y-%m)
  local diff_time=$(date +%s)
  local diff_time=$(echo "$diff_time - $start_time" | bc)
  local diff_time_h=$(echo "$diff_time / 3600" | bc)
  local diff_time_m=$(echo "$diff_time / 60" | bc)
  local diff_time_m=$(echo "$diff_time_m % 60" | bc)
  echo "Duration: $diff_time_h h and $diff_time_m min" | tee -a "$LOG_FILE"
  echo "Successful created: $gh_map_zip" | tee -a "$LOG_FILE"
  
  ##### Store map-file and update json_file and html_file! #####
  if [ ! -z "$SERVER_MAPS_DIR" ]; then
    if $cur_connect "test ! -d \"$SERVER_MAPS_DIR/$ghz_time\"" ; then
      $cur_connect "mkdir \"$SERVER_MAPS_DIR/$ghz_time\""
    fi
    if [ -z "$SERVER_MAPS_REMOTE" ]; then
      mv "$MAP_DIR$gh_map_zip" "$SERVER_MAPS_DIR/$ghz_time/$gh_map_zip"
    else
      echo "Copy map to server" >> "$LOG_FILE"
      scp "$MAP_DIR$gh_map_zip" "$SERVER_MAPS_REMOTE":"$SERVER_MAPS_DIR/$ghz_time/$gh_map_zip"
      RESULT=$?
      sync
      if [ "$RESULT" != "0" ]; then
        echo "Error on uploading: $MAP_DIR$gh_map_zip" | tee -a "$LOG_FILE"
        exit 1
      fi
      rm "$MAP_DIR$gh_map_zip"
    fi
    touch "$MAP_DIR$gh_map_zip"
    
    ##### Update json #####
    local json_line="    { \\\"name\\\": \\\"$gh_map_name\\\", \\\"size\\\": \\\"$ghz_size\\\", \\\"time\\\": \\\"$ghz_time\\\" }"
    local json_file=$(dirname "$SERVER_MAPS_DIR")/map_url-$MAP_REV.json
    local json_key="^    { \\\"name\\\": \\\"$gh_map_name\\\".*,\\\$"
    local json_comma=","
    local json_pre=""
    local has_comma=$($cur_connect "grep \"$json_key\" \"$json_file\"")
    if [ -z "$has_comma" ]; then
      local json_key="^    { \\\"name\\\": \\\"$gh_map_name\\\".*"
      local json_comma=""
      local has_comma=$($cur_connect "grep \"$json_key\" \"$json_file\"")
      if [ -z "$has_comma" ]; then
        local json_key="^  \["
        local json_comma=","
        local json_pre="  [\n"
      fi
    fi
    echo "Replacing in json." | tee -a "$LOG_FILE"
    $cur_connect "sed -i -e \"s#$json_key#$json_pre$json_line$json_comma#g\" \"$json_file\""
    
    ##### Update html #####
    local html_line="    <li><a href=\\\"maps/$ghz_time/$gh_map_zip\\\">$ghz_time $gh_map_zip</a>\\&emsp;size:$ghz_size build_duration=$diff_time_h""h $diff_time_m min</a></li>"
    local html_file=$(dirname "$SERVER_MAPS_DIR")/index.html
    local html_key="^    <li><a href=\\\"maps/[0-9][0-9][0-9][0-9]-[0-9][0-9]/$gh_map_zip\\\".*"
    local html_post=""
    local has_line=$($cur_connect "grep \"$html_key\" \"$html_file\"")
    if [ -z "$has_line" ]; then
      local html_key="^  </body>\\\$"
      local html_post="\\n  </body>"
    fi
    echo "Replacing in html." | tee -a "$LOG_FILE"
    $cur_connect "sed -i -e \"s#$html_key#$html_line$html_post#g\" \"$html_file\""
  fi
}

sort_html()
{
  if [ -z "$SERVER_MAPS_DIR" ]; then
    return
  fi
  if [ ! -z "$SERVER_MAPS_REMOTE" ]; then
    cur_connect="ssh $SERVER_MAPS_REMOTE"
  else
    cur_connect="bash -c"
  fi
  local html_file=$(dirname "$SERVER_MAPS_DIR")/index.html
  local header_splitpos=$($cur_connect "cat \"$html_file\" | grep -v \"<li>.*\\.ghz.*</li>\"" | grep -n "</body>" | cut -d':' -s -f 1)
  echo "Sort html entries." | tee -a "$LOG_FILE"
  echo "Split-pos is: $header_splitpos" | tee -a "$LOG_FILE"
  $cur_connect "cp \"$html_file\" /tmp/index-backup.html" || check_result "$?" "sort_html-backup" #BACKUP
  $cur_connect "cat \"$html_file\" | grep -v \"<li>.*\\.ghz.*</li>\" | head -n $(( $header_splitpos - 1 )) > /tmp/index-new.html" || check_result "$?" "sort_html-head" #HEAD
  $cur_connect "cat \"$html_file\" | grep    \"<li>.*\\.ghz.*</li>\" | sort -k 1.30 >> /tmp/index-new.html" || check_result "$?" "sort_html-lines" #LINES_SORTED
  $cur_connect "cat \"$html_file\" | grep -v \"<li>.*\\.ghz.*</li>\" | tail -n +$header_splitpos >> /tmp/index-new.html" || check_result "$?" "sort_html-tail" #TAIL
  $cur_connect "cat /tmp/index-new.html > \"$html_file\"" #OVERRIDE
}

import_split_box() # Args: lat1,lon1,lat2,lon2 /abs/path/cont_country.osm.pbf subName dlLink bClearDl
{
  local target_name=$(basename "$2" | sed -e "s#.osm.pbf\$#-$3#g")
  if [ "$CONTINUE" = "ask" ]; then
    echo "Finish split_box! Get the maps from $MAP_DIR" | tee -a "$LOG_FILE"
    echo "==================================" | tee -a "$LOG_FILE"
    echo "Starting with map $target_name" | tee -a "$LOG_FILE"
    echo "Subregion:$3" | tee -a "$LOG_FILE"
    echo "Continue? y=yes b=break a=yesToAll"
    echo "          s=skip"
    read -e -p ">>>" ANSWER
    if [ "$ANSWER" = "b" ]; then
      echo "Stop by user, exiting."
      exit 0
    elif [ "$ANSWER" = "a" ]; then
      CONTINUE="yesToAll"
    elif [ "$ANSWER" = "s" ]; then
      echo "Skip $target_name" | tee -a "$LOG_FILE"
      return
    elif [ "$ANSWER" = "y" ]; then
      echo "Continue ..."
    else
      echo "Unknown user input, exiting."
      exit 1
    fi
  else
    echo "==================================" | tee -a "$LOG_FILE"
    echo "Starting with split-box-map $target_name" | tee -a "$LOG_FILE"
    echo "Subregion:$3" | tee -a "$LOG_FILE"
  fi
  if [ ! -e "$2" ]; then
    wget "$4" -O "$2"
  fi
  check_exist "$2"
  import_map_box "$1" "$2" "$3"
  if [ "$5" = "true" ]; then
    rm "$2"
  fi
}

import_continent_polys() # Args europe|europe/germany|europe/great-britain/england
{
  local full_list=$(print_map_list "$1" | cut -d'"' -s -f 2)
  for curUrl in $full_list ; do
    curUrlName=$(echo "$curUrl" | sed -e 's#-latest.osm.pbf#.poly#g')
    curUrlSrc="https://download.openstreetmap.fr/polygons/$curUrlName"
    curUrlTar=$MAP_DIR$(echo "$curUrlName" | tr '/' '_')
    wget -O "$curUrlTar" "$curUrlSrc"
  done
}

import_continent() # Args europe|europe/germany|europe/great-britain/england
{
  local isCountry=$(echo "$1" | grep "/")
  local full_list=$(print_map_list "$1" | cut -d'"' -s -f 2)
  for curUrl in $full_list ; do
    if [ "$CONTINUE" = "ask" ]; then
      echo "Finish! Get the maps from $MAP_DIR" | tee -a "$LOG_FILE"
      echo "==================================" | tee -a "$LOG_FILE"
      date | tee -a "$LOG_FILE"
      echo "Starting with map $curUrl" | tee -a "$LOG_FILE"
      echo "Continue? y=yes b=break a=yesToAll"
      echo "          s=skip w=skipWhooleContinent"
      read -e -p ">>>" ANSWER
      if [ "$ANSWER" = "b" ]; then
        echo "Stop by user, exiting."
        exit 0
      elif [ "$ANSWER" = "a" ]; then
        CONTINUE="yesToAll"
      elif [ "$ANSWER" = "s" ]; then
        echo "Skip $curUrl" | tee -a "$LOG_FILE"
        continue
      elif [ "$ANSWER" = "w" ]; then
        break
      elif [ "$ANSWER" = "y" ]; then
        echo "Continue ..."
      else
        echo "Unknown user input, exiting."
        exit 1
      fi
    else
      echo "Finish!" | tee -a "$LOG_FILE"
      echo "=================================" | tee -a "$LOG_FILE"
      date | tee -a "$LOG_FILE"
      echo "Starting with map $curUrl" | tee -a "$LOG_FILE"
    fi
    if [ -z "$isCountry" ]; then
      import_map "$curUrl"
    else
      local continent=$(echo "$1" | rev | cut -d'/' -s -f 2-99 | rev)
      import_map "$continent/$curUrl"
    fi
  done
}

if [ "$1" = "-i" ]; then
  echo "Starting ..."
elif [ "$1" = "-si" ]; then
  SERVER_MAPS_DIR="$SERVER_MAPS_DIR_DEFAULT"
elif [ "$1" = "-s" ]; then
  SERVER_MAPS_DIR="$SERVER_MAPS_DIR_DEFAULT"
  CONTINUE="yesToAll"
elif [ "$1" = "-g" ]; then
  import_map_box "$2" "$3" "$4"
  echo "Finish! Get the maps from $MAP_DIR" | tee -a "$LOG_FILE"
  exit 0
elif [ "$1" = "-p" ]; then
  import_continent_polys europe
  import_continent_polys africa
  import_continent_polys asia
  import_continent_polys australia-oceania
  import_continent_polys central-america
  import_continent_polys north-america
  import_continent_polys south-america
  import_continent_polys russia
  import_continent_polys europe/germany
  import_continent_polys europe/italy
  import_continent_polys europe/france
  import_continent_polys north-america/canada
  import_continent_polys north-america/us
  exit 0
else
  echo "The server mode ensures to update html-file and json-list-file."
  echo "Also server mode copies the maps to server path: SERVER_MAPS_DIR_DEFAULT"
  echo "Interactive asks for each map to import, skip or importAll"
  echo "======================================="
  echo "Usage:"
  echo "For interactive mode enter: $0 -i"
  echo "For server mode enter: $0 -s"
  echo "For interactive server mode enter: $0 -si"
  echo "For import desired geo-box enter:"
  echo "     -g top,left,bottom,right /abs/path/cont_country.osm.pbf subName"
  echo "     (example: -g 5.2,-74.0,-13.7,-46.0 /tmp/south-america_brazil.osm.pbf north)"
  echo "For just importing polys use -p"
  exit 0
fi

### Skip files that are too big at once ###
touch "$MAP_DIR/russia.ghz"
touch "$MAP_DIR/europe_germany.ghz"
touch "$MAP_DIR/europe_italy.ghz"
touch "$MAP_DIR/europe_france.ghz"
touch "$MAP_DIR/north-america_canada.ghz"
touch "$MAP_DIR/north-america_us.ghz"
touch "$MAP_DIR/south-america_brazil.ghz"

### Skip faulty map files ###
touch "$MAP_DIR/australia-oceania_ile-de-clipperton.ghz"
touch "$MAP_DIR/australia-oceania_pitcairn-islands.ghz"
touch "$MAP_DIR/australia-oceania_tokelau.ghz"
touch "$MAP_DIR/australia-oceania_tuvalu.ghz"

### Start imports ###
import_continent europe
import_continent africa
import_continent asia
import_continent australia-oceania
import_continent central-america
import_continent north-america
import_continent south-america
import_continent russia
import_continent europe/germany
import_continent europe/italy
import_continent europe/france
import_continent north-america/canada
import_continent north-america/us
import_split_box -21.9,-57.8,-33.8,-47.7 /tmp/south-america_brazil.osm.pbf s "$LINK_BRAZIL" false
import_split_box -14.2,-52.9,-25.5,-39.5 /tmp/south-america_brazil.osm.pbf se "$LINK_BRAZIL" false
import_split_box  -0.7,-48.4,-18.3,-34.6 /tmp/south-america_brazil.osm.pbf ne "$LINK_BRAZIL" false
import_split_box   5.2,-74.0,-13.7,-46.0 /tmp/south-america_brazil.osm.pbf n "$LINK_BRAZIL" false
import_split_box  -7.4,-61.5,-24.0,-45.9 /tmp/south-america_brazil.osm.pbf cw "$LINK_BRAZIL" true
import_split_box 54.0,72.4,14.0,95.0 /tmp/asia_china.osm.pbf west "$LINK_CHINA" false
import_split_box 54.0,95.0,14.0,115.0 /tmp/asia_china.osm.pbf center "$LINK_CHINA" false
import_split_box 54.0,115.0,14.0,136.0 /tmp/asia_china.osm.pbf east "$LINK_CHINA" true
import_split_box 44.0,-9.7,40.0,5.0 /tmp/europe_spain.osm.pbf north "$LINK_SPAIN" false # Bound top < bottom
import_split_box 40.0,-9.7,35.0,5.0 /tmp/europe_spain.osm.pbf south "$LINK_SPAIN" true

sort_html

echo "Finish! Get the maps from $MAP_DIR"
