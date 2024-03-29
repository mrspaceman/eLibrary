## Android EBook Library Manager

Allow you to manage your ebook collection directly on your android device.
To use this app, create a directory on your device and
copy your ebooks into this directory 
(using folders to categorise the ebooks if you wish).

This app will not open your ebooks. it only allows you to manage them.
To open your ebooks you will need to install a reader app such as FBReader,
Adobe PDF reader, Google Books etc...
 

### Features
    * [x] Add multiple library root directories
    * [x] scan files under list of root dirs
    * [x] support pdf and epub
    * [x] get cover images from epub (if set)
    * [x] grab first page of PDF and use as cover image
    * [x] add directory ebook file is in as a tag
    * [x] filter the ebooks displayed by tag selected
    * [x] search for ebooks with a string in the title/bookTags/directory
    * [x] convert to kotlin
    * [x] Convert to Room DB

### Todo
    * [ ] download covers/metadata from
        * https://www.librarything.com/services/
        * https://developers.google.com/books/docs/v1/reference/
        * https://www.goodreads.com/api
        * https://openlibrary.org/dev/docs/api/covers
    * [ ] pick a cover image from local storage
    * [ ] add/edit bookTags associated with your ebooks
    * [ ] update the title for your ebooks
    * [ ] update the summary for your ebooks
    * [ ] show tag list in picker dialog as a tree
    * [ ] Add support for mobi files
    * [ ] Add support for cbt files
    * [ ] allow marking ebooks as [read|reading|unread|new]
    * [ ] Watch file system for changes to ebook libraries
    * [ ] periodic [daily|weekly] scan of root dirs in background


## For debugging (on tablet):

```
adb -s 88c859cacec5cc73 shell setprop log.tag.BookLibApplication VERBOSE
adb -s 88c859cacec5cc73 shell setprop debug.firebase.analytics.app uk.co.droidinactu.elibrary
adb -s 88c859cacec5cc73 shell setprop log.tag.FA VERBOSE


adb -s 88c859cacec5cc73 shell

setprop log.tag.BookLibApplication VERBOSE
setprop debug.firebase.analytics.app uk.co.droidinactu.elibrary
setprop log.tag.FA VERBOSE


getprop log.tag.BookLibApplication

getprop debug.firebase.analytics.app

getprop log.tag.FA

```
