# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class org.apache.poi.** { *; }
-keep class javax.xml.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.schemas.** { *; }


-dontwarn javax.xml.stream.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**
-dontwarn org.bouncycastle.**
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.jcp.xml.dsig.internal.dom.**
-dontwarn org.apache.tools.ant.**
-dontwarn com.github.javaparser.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.w3c.dom.svg.**
-dontwarn org.apache.pdfbox.**
-dontwarn de.rototor.pdfbox.**
-dontwarn org.apache.xml.security.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.ietf.jgss.**
-dontwarn org.w3c.dom.events.**
-dontwarn org.w3c.dom.traversal.**
-dontwarn org.apache.maven.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn com.sun.org.apache.xml.internal.resolver.**
-dontwarn org.osgi.framework.**