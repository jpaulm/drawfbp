<?xml version='1.0' encoding='ISO-8859-1' ?>

<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd" >

<helpset version="2.0" >
   <!-- title 
   <title>DrawFBP Help</title>
   --> 
   			
   <!-- maps --> 
   <maps>
     <homeID>overview</homeID>

     <mapref location="help/map.jhm" />

   </maps>
	
   <!-- views --> 
   <view>
      <name>TOC</name>
      <label>Table Of Contents</label>
      <type>javax.help.TOCView</type>

      <data>help/helptoc.xml</data>

      
   </view>
 
   <!--	
   <view>
      <name>Index</name>
      <label>Index</label>
      <type>javax.help.IndexView</type>

      <data>help/helpindex.xml</data>

   </view> 
   
	
   <view>
      <name>Search</name>
      <label>Search</label>
      <type>javax.help.SearchView</type>
         <data engine="com.sun.java.help.search.DefaultSearchEngine">
         JavaHelpSearch
         </data>

   </view>
   -->

   <!-- A glossary navigator  - no glossary right now
   <view>
      <name>glossary</name>
      <label>Glossary</label>
      <type>javax.help.GlossaryView</type>

      <data>help/glossary.xml</data>

   </view>
    -->

   <!-- A favorites navigator - seems to crash CSH facility...
   <view >
      <name>favorites</name>
      <label>Favorites</label>
      <type>javax.help.FavoritesView</type>

   </view>     -->

   <!-- presentation windows -->

   <!-- This window is the default one for the helpset. 
     *  Its title bar has been changed. It
     *  is a tri-paned window because displayviews, not
     *  defined, defaults to true and because a toolbar is defined.
     *  The toolbar has a back arrow, a forward arrow, and
     *  a home button that has a user-defined image.
   -->
   <presentation default=true>
       <name>main window</name>
       <size width="650" height="500" /> 
       <location x="100" y="50" />
       <title>DrawFBP Help</title>
       <image>drawfbplogo</image>
       <toolbar>
           <helpaction>javax.help.BackAction</helpaction>
           <helpaction>javax.help.ForwardAction</helpaction>
           <helpaction image="homeicon">javax.help.HomeAction</helpaction>
       </toolbar>
    </presentation>

   <!-- This window is simpler than the main window. 
     *  It's intended to be used a secondary window.
     *  It has no navigation pane or toolbar.
   -->
   <presentation displayviews=false>
       <name>secondary window</name>
       <size width="300" height="500" /> 
       <location x="100" y="50" />
       <image>drawfbplogo</image>
   </presentation>
 
   <!-- subhelpsets --> 
   <!--subhelpset location="file:/c:/Foobar/HelpSet2.hs" /-->

   <!-- implementation section   -->
   <impl>
      <helpsetregistry helpbrokerclass="javax.help.DefaultHelpBroker" />
      <viewerregistry viewertype="text/html" 
         viewerclass="com.sun.java.help.impl.CustomKit" />
     <!-- <viewerregistry viewertype="text/xml" 
         viewerclass="com.sun.java.help.impl.CustomXMLKit" />     
        <viewerregistry viewertype="application/pdf" viewerclass="Your PDF Editor Kit" /> -->
   </impl> 
</helpset>