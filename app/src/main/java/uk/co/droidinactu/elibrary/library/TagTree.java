package uk.co.droidinactu.booklib.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagTree {

    Map<Long, Tag> tagMap = new HashMap<>();
    public List<Tag> rootTags = new ArrayList<>();

    public void add(BookTag t) {
        Tag newT = null;
        if (tagMap.containsKey(t.getId())) {
            newT = tagMap.get(t.getId());
        } else {
            newT = new Tag();
            newT.me = t;
            tagMap.put(t.getId(), newT);
        }

        if (newT.me.getParentTagId() == null) {
            rootTags.add(newT);
        } else {
            Tag pt = tagMap.get(newT.me.getParentTagId());
            pt.children.add(newT);
        }
    }

    public void remove(String currentlyReading) {
    }

    public class Tag {
        public BookTag me;
        public List<Tag> children = new ArrayList<>();
    }


}
