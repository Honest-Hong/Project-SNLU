package com.snlu.snluapp.item;

import java.util.ArrayList;

/**
 * Created by Winlab 5 on 2017-05-29.
 */

public class SummaryContentItem {
    private String name;
    private ArrayList<SentenceItem> sentenceItems;

    public SummaryContentItem(String name, ArrayList<SentenceItem> sentenceItems) {
        this.name = name;
        this.sentenceItems = sentenceItems;
    }

    public SummaryContentItem() {
        name = "빈 폴더";
        sentenceItems = new ArrayList<>();
        SentenceItem item = new SentenceItem();
        item.setSentence("발언을 드래그하여 추가하세요");
        sentenceItems.add(item);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SentenceItem> getSentenceItems() {
        return sentenceItems;
    }

    public void setSentenceItems(ArrayList<SentenceItem> sentenceItems) {
        this.sentenceItems = sentenceItems;
    }
}
