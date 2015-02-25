#! /bin/sh

if [ $# -eq 0 ]
then
	echo "Usage: $0 <number_of_db_version>"
	exit 1
fi

filename="db_$1.sql"
touch $filename
linenum=$(grep -n $(echo $1) current_db.sql | grep -Eo '^[^:]+')
sed -n "1, $linenum p" current_db.sql >> $filename
