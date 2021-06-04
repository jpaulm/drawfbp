echo off
echo DrawFBP displaying "%1"
echo Jar file: "%DRAWFBP_LIB%\%DRAWFBP_VERSION%.jar"
cd "%DRAWFBP_LIB%"
%JAVA_HOME%\bin\java.exe -jar %DRAWFBP_VERSION%.jar %1
pause