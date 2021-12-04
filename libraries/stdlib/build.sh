# TODO: Temporary

cd ../../compiler || exit

./gradlew build distZip -x test &&
  cd ./build/distributions/ &&
  unzip -o dotlin-0.0.1 &&
  dotlin-0.0.1/bin/dotlin ../../../libraries/stdlib/src/kotlin stdlib.klib
