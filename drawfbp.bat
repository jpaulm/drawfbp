echo off
echo DrawFBP displaying "%1"
echo Jar file: "%DRAWFBP_LIB%\%DRAWFBP_VERSION%.jar"
pause
"%JAVA_HOME%\bin\javaw.exe" -jar "%DRAWFBP_LIB%\build\libs\%DRAWFBP_VERSION%.jar" %1
