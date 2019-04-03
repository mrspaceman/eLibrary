# ebookmgr

[![AppCentreBuild](https://build.appcenter.ms/v0.1/apps/6603308e-b77f-4403-ad2a-0b4ffa3f963a/branches/master/badge)](https://build.appcenter.ms/v0.1/apps/6603308e-b77f-4403-ad2a-0b4ffa3f963a/branches/master/badge)

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


### Todo
    * [ ] convert to kotlin
    * [ ] Convert to Room DB
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



## For debugging:

```
adb shell
setprop log.tag.BookLibApplication VERBOSE
```
