#!/bin/bash
trap 'cleanup; exit' 1 2 3 6


function cleanup() {
    rm -f temp.txt
    close_data_connection
    close_controll_connection
    cleanup
    exit 
}

function close_controll_connection(){
    #echo "closing controll"
    # Κλείσε το output redirection για το socket
    exec 3>&-
    # Κλείσε το input redirection για το socket
    exec 3<&-
}

function close_data_connection(){
    #echo "closing data"
    exec 5>&-
    exec 5<&-
}

function start_data_connection(){
    echo PASV >&3
    read line <&3
    #echo $line

    ipaddress=$(echo "$line" | cut -d " " -f 5 | sed  's/[().]//g'  | awk -F "," '{print $1"."$2"."$3"."$4}')
    ipaddress=$(echo "$line" | cut -d " " -f 5 | sed  -e 's/[().]//g' -e 's/^M//g' | awk -F "," '{print $1"."$2"."$3"."$4}')

    p1=$(echo "$line" | cut -d " " -f 5 | tr ')' ','   | awk -F"," '{print $5}')
    p2=$(echo "$line" | cut -d " " -f 5 | tr ')' ','   | awk -F"," '{print $6}')




    Dec2Bin=({0..1}{0..1}{0..1}{0..1}{0..1}{0..1}{0..1}{0..1}); 

    binary=$(echo ${Dec2Bin[$p1]}${Dec2Bin[$p2]})
    port=`echo "$((2#$binary))"`



    exec 5<>/dev/tcp/$ipaddress/$port

}
function login(){
    exec 3<>/dev/tcp/$localhost/21
    read line <&3

    echo USER $ftpadmin >&3
    read line <&3

    echo -n "Please specify the password : "
    read password

    echo PASS $password >&3
    read line <&3
    #echo $line
}

function create_dirs(){
    old_IFS=$IFS
    IFS=$'\n'
    var=`find $pathname -type d | sed -e "s:"$pathname/"::g"   -e "s:"$pathname"::g" `
    for i in $var; 
    do
        echo "MKD "$i""  >&3
        read line <&3
        #echo $line > temp.txt
    done
    IFS=$old_IFS
}

if [ $# -lt "3" ];then

  echo "Wrong Command Line. Format: ./ftpupload filedir ftpserver username"
  # Abort the script and return a non-zero exit status.
  cleanup
  exit 1
fi

if [[ ! -d $1 ]];then
    
  echo $1 "is not a directory"
  #Abort the script and return a non-zero exit status.
  cleanup
  exit 1
fi



pathname=$1
localhost=$2
ftpadmin=$3



if [ "${pathname: -1}" == '/' ];then
    pathname=$(echo $pathname | sed 's:/$::' )

fi



login
create_dirs 




old_IFS=$IFS
IFS=$'\n'

for i in `find $pathname -type d `; 
do
    if [[  $(ls -al $i | egrep -v "^d|^total" | wc -l) -gt 0 ]];
    then

        host_Pathname=$(echo $i | sed -e "s:"$pathname"::g" -e 's:/::' -e 's:$:/:' )
        old_IFS=$IFS
        IFS=$'\n'
        for j in `find $i -maxdepth 1  -type f `
        do

            if echo $j | egrep ".jpeg$|.jpg$|.bmp$|.gif$|.png$|.apng$|.gif$|.svg$|.avif$|.bmp$|.ico$|.cur$|.tif" > temp.txt ; 
            then
                #echo "in second if"
                start_data_connection
    

                echo "TYPE I"  >&3
                read line <&3
                #echo $line
                filename=$(echo $j | awk -F "/" '{print $NF}')
                echo "STOR "$host_Pathname$filename""   >&3
                read line <&3

                if echo $line | grep "^553" > temp.txt
                then
                    echo "STOR $filename"   >&3
                    read line <&3
                    #echo $line
                fi
                
                #echo $line
                
                cat  "$j" >&5 
            

                
                

                close_data_connection
                #echo "done from second if"
                read line <&3 
                #echo $line    
                        
        
            else 
                #echo "in first if"
                start_data_connection
                filename=$(echo $j | awk -F "/" '{print $NF}')    

                
                echo "TYPE A"  >&3
                read line <&3
                #echo $line
                #echo "STOR $host_Pathname$filename" >>commands.txt
                echo "STOR $host_Pathname$filename"   >&3
                read line <&3
                #echo $line

                if echo $line | grep "^553" > temp.txt
                then
                    echo "STOR $filename"   >&3
                    read line <&3
                    #echo $line
                fi

               
                cat "$j" >&5

                close_data_connection
                #echo "done from first if"
                read line <&3 
                #echo $line    
                # echo $j >> commands.txt
                # echo "" >> commands.txt
            fi

        done
        
        IFS=$old_IFS
        fi
    
done





rm -f temp.txt
close_controll_connection







