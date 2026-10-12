#!/bin/bash
set -e
SDK=/opt/android-sdk
APP=/root/Win7Lite/app
BT=$SDK/build-tools/34.0.0
PLATFORM=$SDK/platforms/android-34/android.jar
OUT=/root/Win7Lite/out
QEMU=qemu-x86_64-static
AAPT2="$QEMU $BT/aapt2"
ZIPALIGN="$QEMU $BT/zipalign"

rm -rf $OUT && mkdir -p $OUT/gen $OUT/classes

# 1. 编译资源
$AAPT2 compile --dir $APP/src/main/res -o $OUT/res.zip

# 2. 链接资源 + 生成 R.java
$AAPT2 link -o $OUT/base.apk \
  -I $PLATFORM \
  --manifest $APP/src/main/AndroidManifest.xml \
  --java $OUT/gen \
  $OUT/res.zip

# 3. 编译 R.java + 源码
javac -source 1.8 -target 1.8 \
  -classpath $PLATFORM \
  -d $OUT/classes \
  $OUT/gen/com/win7lite/R.java \
  $(find $APP/src/main/java -name '*.java')

# 4. 转 dex
$BT/d8 --release --lib $PLATFORM \
  --output $OUT \
  $(find $OUT/classes -name '*.class')

ls -la $OUT/classes.dex

# 5. 把 dex 塞进 apk（放在根目录）
cd $OUT
cp classes.dex /tmp/classes.dex
zip -q -j base.apk classes.dex

# 6. 对齐
$ZIPALIGN -f 4 base.apk aligned.apk

# 7. 签名
keytool -genkeypair -keystore $OUT/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000 2>/dev/null || true
$BT/apksigner sign --ks $OUT/debug.keystore --ks-pass pass:android --key-pass pass:android --out /root/Win7Lite/Win7Lite.apk aligned.apk

echo "=== BUILD SUCCESS ==="
ls -la /root/Win7Lite/Win7Lite.apk