git log --oneline -5 > log.txt
./gradlew clean
./gradlew assembleRelease
#./gradlew bundleRelease
firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk  \
--app 1:541715569412:android:c5e8e87768507952544e51  \
--release-notes-file ./log.txt --groups ViewGO