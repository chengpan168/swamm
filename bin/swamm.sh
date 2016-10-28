#!/usr/bin/env bash

if [ $# != 3 ]; then
    echo "输入参数错误！"
    echo "示例：swamm.sh class=UserApiService(接口名，可以带号分隔) src/main/java(源码位置) com.shining3d.pangu.client(包名)"
    exit 0;
else
    echo ""

fi

BASE_DIR=`cd "$(dirname "$0")"; pwd`
BASE_DIR=`dirname $BASE_DIR`

echo "swamm 根目录：$BASE_DIR"

DOCLET_PATH=""
for jar in `ls $BASE_DIR/lib`
do
    DOCLET_PATH="$BASE_DIR/lib/$jar:$DOCLET_PATH"
done

#DOCLET_PATH="$BASE_DIR/target/swamm-1.0.jar:$DOCLET_PATH"

#echo "doclet path：$DOCLET_PATH"

echo "javadoc -tag $1 -cp $2 -doclet com.swamm.doc.Doclet -docletpath $DOCLET_PATH -subpackages $3"

javadoc -tag $1 -cp $2 -doclet com.swamm.doc.Doclet -docletpath $DOCLET_PATH  -private -subpackages $3

#javadoc -tag class=ClassifyApiService  -cp src/main/java
# -doclet com.swamm.doc.Doclet -docletpath ../swamm/target/swamm-1.0.jar:../swamm/lib/commons-lang3-3.4.jar:../swamm/lib/fastjson-1.2.12.jar:
# -private  -subpackages com.shining3d.pangu.client