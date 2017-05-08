VERSION=`./gradlew version | grep Version | cut -f 2 -d " "`
PASSPHRASE=`cat ~/.gnupg/passphrase.txt`
GPGPARAMS="--passphrase $PASSPHRASE --batch --yes --no-tty"
./gradlew assemble generatePom

echo
echo CORE
echo

mv kanvas-core/build/libs/kanvas-core.jar kanvas-core/build/libs/kanvas-core-${VERSION}.jar
mv kanvas-core/build/libs/kanvas-core-javadoc.jar kanvas-core/build/libs/kanvas-core-${VERSION}-javadoc.jar
mv kanvas-core/build/libs/kanvas-core-sources.jar kanvas-core/build/libs/kanvas-core-${VERSION}-sources.jar
sed s/unspecified/$VERSION/g kanvas-core/build/pom.xml > kanvas-core/build/pom_corrected.xml
mv kanvas-core/build/pom_corrected.xml kanvas-core/build/pom.xml
gpg $GPGPARAMS -ab kanvas-core/build/pom.xml
gpg $GPGPARAMS -ab kanvas-core/build/libs/kanvas-core-${VERSION}.jar
gpg $GPGPARAMS -ab kanvas-core/build/libs/kanvas-core-${VERSION}-javadoc.jar
gpg $GPGPARAMS -ab kanvas-core/build/libs/kanvas-core-${VERSION}-sources.jar
cd kanvas-core/build/libs
jar -cvf bundle-kanvas-core.jar ../pom.xml ../pom.xml.asc kanvas-core-${VERSION}.jar kanvas-core-${VERSION}.jar.asc kanvas-core-${VERSION}-javadoc.jar kanvas-core-${VERSION}-javadoc.jar.asc kanvas-core-${VERSION}-sources.jar kanvas-core-${VERSION}-sources.jar.asc
cd ../../..

echo
echo PYTHON
echo

mv kanvas-python/build/libs/kanvas-python.jar kanvas-python/build/libs/kanvas-python-${VERSION}.jar
mv kanvas-python/build/libs/kanvas-python-javadoc.jar kanvas-python/build/libs/kanvas-python-${VERSION}-javadoc.jar
mv kanvas-python/build/libs/kanvas-python-sources.jar kanvas-python/build/libs/kanvas-python-${VERSION}-sources.jar
sed s/unspecified/$VERSION/g kanvas-python/build/pom.xml > kanvas-python/build/pom_corrected.xml
mv kanvas-python/build/pom_corrected.xml kanvas-python/build/pom.xml
gpg $GPGPARAMS -ab kanvas-python/build/pom.xml
gpg $GPGPARAMS -ab kanvas-python/build/libs/kanvas-python-${VERSION}.jar
gpg $GPGPARAMS -ab kanvas-python/build/libs/kanvas-python-${VERSION}-javadoc.jar
gpg $GPGPARAMS -ab kanvas-python/build/libs/kanvas-python-${VERSION}-sources.jar
cd kanvas-python/build/libs
jar -cvf bundle-kanvas-python.jar ../pom.xml ../pom.xml.asc kanvas-python-${VERSION}.jar kanvas-python-${VERSION}.jar.asc kanvas-python-${VERSION}-javadoc.jar kanvas-python-${VERSION}-javadoc.jar.asc kanvas-python-${VERSION}-sources.jar kanvas-python-${VERSION}-sources.jar.asc
cd ../../..

echo
echo SANDY
echo

mv kanvas-sandy/build/libs/kanvas-sandy.jar kanvas-sandy/build/libs/kanvas-sandy-${VERSION}.jar
mv kanvas-sandy/build/libs/kanvas-sandy-javadoc.jar kanvas-sandy/build/libs/kanvas-sandy-${VERSION}-javadoc.jar
mv kanvas-sandy/build/libs/kanvas-sandy-sources.jar kanvas-sandy/build/libs/kanvas-sandy-${VERSION}-sources.jar
sed s/unspecified/$VERSION/g kanvas-sandy/build/pom.xml > kanvas-sandy/build/pom_corrected.xml
mv kanvas-sandy/build/pom_corrected.xml kanvas-sandy/build/pom.xml
gpg $GPGPARAMS -ab kanvas-sandy/build/pom.xml
gpg $GPGPARAMS -ab kanvas-sandy/build/libs/kanvas-sandy-${VERSION}.jar
gpg $GPGPARAMS -ab kanvas-sandy/build/libs/kanvas-sandy-${VERSION}-javadoc.jar
gpg $GPGPARAMS -ab kanvas-sandy/build/libs/kanvas-sandy-${VERSION}-sources.jar
cd kanvas-sandy/build/libs
jar -cvf bundle-kanvas-sandy.jar ../pom.xml ../pom.xml.asc kanvas-sandy-${VERSION}.jar kanvas-sandy-${VERSION}.jar.asc kanvas-sandy-${VERSION}-javadoc.jar kanvas-sandy-${VERSION}-javadoc.jar.asc kanvas-sandy-${VERSION}-sources.jar kanvas-sandy-${VERSION}-sources.jar.asc
cd ../../..

mkdir -p release
mv kanvas-core/build/libs/bundle-kanvas-core.jar release
mv kanvas-python/build/libs/bundle-kanvas-python.jar release
mv kanvas-sandy/build/libs/bundle-kanvas-sandy.jar release