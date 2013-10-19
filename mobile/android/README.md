# Building with NDK r9 and later

A change in the Android NDK compiler settings that was introduced in version r9 has
broken module builds under 3.1.3.GA and earlier.  According to 
[this bug](https://jira.appcelerator.org/browse/TIMOB-15263), the fix is to add the
following lines to `$TITANIUM_HOME/mobilesdk/osx/3.1.3.GA/module/android/generated/Android.mk`:

    # https://jira.appcelerator.org/browse/TIMOB-15263
    LOCAL_DISABLE_FORMAT_STRING_CHECKS=true
    
I haven't found a means of adding this parameter that doesn't involve editing the Ti SDK
file yet.